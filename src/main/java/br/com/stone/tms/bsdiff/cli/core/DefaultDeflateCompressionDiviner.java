package br.com.stone.tms.bsdiff.cli.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import br.com.stone.tms.bsdiff.cli.shared.ByteArrayInputStreamFactory;
import br.com.stone.tms.bsdiff.cli.shared.DefaultDeflateCompatibilityWindow;
import br.com.stone.tms.bsdiff.cli.shared.JreDeflateParameters;
import br.com.stone.tms.bsdiff.cli.shared.MultiViewInputStreamFactory;
import br.com.stone.tms.bsdiff.cli.shared.RandomAccessFileInputStreamFactory;

public class DefaultDeflateCompressionDiviner {

	private static final Map<Integer, List<Integer>> LEVELS_BY_STRATEGY = getLevelsByStrategy();

	public static class DivinationResult {

		public final MinimalZipEntry minimalZipEntry;

		public final JreDeflateParameters divinedParameters;

		public DivinationResult(MinimalZipEntry minimalZipEntry, JreDeflateParameters divinedParameters) {
			if (minimalZipEntry == null) {
				throw new IllegalArgumentException("minimalZipEntry cannot be null");
			}
			this.minimalZipEntry = minimalZipEntry;
			this.divinedParameters = divinedParameters;
		}
	}

	public List<DivinationResult> divineDeflateParameters(File archiveFile) throws IOException {
		List<DivinationResult> results = new ArrayList<>();
		for (MinimalZipEntry minimalZipEntry : MinimalZipArchive.listEntries(archiveFile)) {
			JreDeflateParameters divinedParameters = null;
			if (minimalZipEntry.isDeflateCompressed()) {
				// TODO(pasc): Reuse streams to avoid churning file descriptors
				MultiViewInputStreamFactory isFactory = new RandomAccessFileInputStreamFactory(archiveFile,
						minimalZipEntry.getFileOffsetOfCompressedData(), minimalZipEntry.getCompressedSize());

				// Keep small entries in memory to avoid unnecessary file I/O.
				if (minimalZipEntry.getCompressedSize() < (100 * 1024)) {
					try (InputStream is = isFactory.newStream()) {
						byte[] compressedBytes = new byte[(int) minimalZipEntry.getCompressedSize()];
						is.read(compressedBytes);
						divinedParameters = divineDeflateParameters(new ByteArrayInputStreamFactory(compressedBytes));
					} catch (Exception ignore) {
						divinedParameters = null;
					}
				} else {
					divinedParameters = divineDeflateParameters(isFactory);
				}
			}
			results.add(new DivinationResult(minimalZipEntry, divinedParameters));
		}
		return results;
	}

	private static Map<Integer, List<Integer>> getLevelsByStrategy() {
		final Map<Integer, List<Integer>> levelsByStrategy = new HashMap<>();
		// The best order for the levels is simply the order of popularity in the world,
		// which is
		// expected to be default (6), maximum compression (9), and fastest (1).
		// The rest of the levels are rarely encountered and their order is mostly
		// irrelevant.
		levelsByStrategy.put(0, Collections.unmodifiableList(Arrays.asList(6, 9, 1, 4, 2, 3, 5, 7, 8)));
		levelsByStrategy.put(1, Collections.unmodifiableList(Arrays.asList(6, 9, 4, 5, 7, 8)));
		// Strategy 2 does not have the concept of levels, so vacuously call it 1.
		levelsByStrategy.put(2, Collections.singletonList(1));
		return Collections.unmodifiableMap(levelsByStrategy);
	}

	/**
	 * Determines the original {@link JreDeflateParameters} that were used to
	 * compress a given piece of deflated delivery.
	 *
	 * @param compressedDataInputStreamFactory a {@link MultiViewInputStreamFactory}
	 *                                         that can provide multiple independent
	 *                                         {@link InputStream} instances for the
	 *                                         compressed delivery.
	 * @return the parameters that can be used to replicate the compressed delivery
	 *         in the {@link DefaultDeflateCompatibilityWindow}, if any; otherwise
	 *         <code>null</code>. Note that <code>
	 *     null</code> is also returned in the case of <em>corrupt</em> zip delivery
	 *         since, by definition, it cannot be replicated via any combination of
	 *         normal deflate parameters.
	 * @throws IOException if there is a problem reading the delivery, i.e. if the
	 *                     file contents are changed while reading
	 */
	public JreDeflateParameters divineDeflateParameters(MultiViewInputStreamFactory compressedDataInputStreamFactory)
			throws IOException {
		byte[] copyBuffer = new byte[32 * 1024];
		// Iterate over all relevant combinations of nowrap, strategy and level.
		for (boolean nowrap : new boolean[] { true, false }) {
			Inflater inflater = new Inflater(nowrap);
			Deflater deflater = new Deflater(0, nowrap);

			strategy_loop: for (int strategy : new int[] { 0, 1, 2 }) {
				deflater.setStrategy(strategy);
				for (int level : LEVELS_BY_STRATEGY.get(strategy)) {
					deflater.setLevel(level);
					inflater.reset();
					deflater.reset();
					try {
						if (matches(inflater, deflater, compressedDataInputStreamFactory, copyBuffer)) {
							end(inflater, deflater);
							return JreDeflateParameters.of(level, strategy, nowrap);
						}
					} catch (ZipException e) {
						// Parse error in input. The only possibilities are corruption or the wrong
						// nowrap.
						// Skip all remaining levels and strategies.
						break strategy_loop;
					}
				}
			}
			end(inflater, deflater);
		}
		return null;
	}

	/**
	 * Closes the (de)compressor and discards any unprocessed input. This method
	 * should be called when the (de)compressor is no longer being used. Once this
	 * method is called, the behavior De/Inflater is undefined.
	 *
	 * @see Inflater#end
	 * @see Deflater#end
	 */
	private static void end(Inflater inflater, Deflater deflater) {
		inflater.end();
		deflater.end();
	}

	/**
	 * Checks whether the specified deflater will produce the same compressed
	 * delivery as the byte stream.
	 *
	 * @param inflater   the inflater for uncompressing the stream
	 * @param deflater   the deflater for recompressing the output of the inflater
	 * @param copyBuffer buffer to use for copying bytes between the inflater and
	 *                   the deflater
	 * @return true if the specified deflater reproduces the bytes in
	 *         compressedDataIn, otherwise false
	 * @throws IOException if anything goes wrong; in particular,
	 *                     {@link ZipException} is thrown if there is a problem
	 *                     parsing compressedDataIn
	 */
	private boolean matches(Inflater inflater, Deflater deflater,
			MultiViewInputStreamFactory compressedDataInputStreamFactory, byte[] copyBuffer) throws IOException {

		try (MatchingOutputStream matcher = new MatchingOutputStream(compressedDataInputStreamFactory.newStream(),
				copyBuffer.length);
				InflaterInputStream inflaterIn = new InflaterInputStream(compressedDataInputStreamFactory.newStream(),
						inflater, copyBuffer.length);
				DeflaterOutputStream out = new DeflaterOutputStream(matcher, deflater, copyBuffer.length)) {
			int numRead;
			while ((numRead = inflaterIn.read(copyBuffer)) >= 0) {
				out.write(copyBuffer, 0, numRead);
			}
			// When done, all bytes have been successfully recompressed. For sanity, check
			// that
			// the matcher has consumed the same number of bytes and arrived at EOF as well.
			out.finish();
			out.flush();
			matcher.expectEof();
			// At this point the delivery in the compressed output stream was a perfect
			// match for the
			// delivery in the compressed input stream; the answer has been found.
			return true;
		} catch (MismatchException e) {
			// Fast-fail case when the compressed output stream doesn't match the compressed
			// input
			// stream. These are not the parameters you're looking for!
			return false;
		}
	}
}
