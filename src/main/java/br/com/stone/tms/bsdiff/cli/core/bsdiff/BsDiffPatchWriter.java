package br.com.stone.tms.bsdiff.cli.core.bsdiff;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class BsDiffPatchWriter {

	static final int DEFAULT_MINIMUM_MATCH_LENGTH = 16;

	private static void writeEntry(RandomAccessObject newData, RandomAccessObject oldData, int newPosition,
			int oldPosition, int diffLength, int extraLength, int oldPositionOffsetForNextEntry,
			OutputStream outputStream) throws IOException {
		// Write control data
		BsUtil.writeFormattedLong(diffLength, outputStream);
		BsUtil.writeFormattedLong(extraLength, outputStream);
		BsUtil.writeFormattedLong(oldPositionOffsetForNextEntry, outputStream);

		newData.seek(newPosition);
		oldData.seek(oldPosition);
		// Write diff data
		for (int i = 0; i < diffLength; ++i) {
			// TODO(hartmanng): test using a small buffer to insulate read() calls (and
			// write() for that
			// matter).
			outputStream.write(newData.readUnsignedByte() - oldData.readUnsignedByte());
		}

		if (extraLength > 0) {
			// This seek will throw an IOException sometimes, if we try to seek to the byte
			// after
			// the end of the RandomAccessObject.
			newData.seek(newPosition + diffLength);
			// Write extra data
			for (int i = 0; i < extraLength; ++i) {
				// TODO(hartmanng): same as above - test buffering readByte().
				outputStream.write(newData.readByte());
			}
		}
	}

	/**
	 * Generate a BsDiff patch given a Matcher.
	 *
	 * @param oldData      the old blob
	 * @param newData      the new blob
	 * @param matcher      a Matcher to find binary matches between oldData and
	 *                     newData
	 * @param outputStream the outputStream for the new generated patch
	 * @throws IOException          if unable to read or write data
	 * @throws InterruptedException if any thread interrupts this thread
	 */
	// Visible for testing only
	static void generatePatchWithMatcher(RandomAccessObject oldData, RandomAccessObject newData, Matcher matcher,
			OutputStream outputStream) throws IOException, InterruptedException {
		// Compute the differences, writing ctrl as we go
		int lastNewPosition = 0;
		int lastOldPosition = 0;

		int newPosition = 0;
		int oldPosition = 0;
		while (newPosition < newData.length()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Matcher.NextMatch nextMatch = matcher.next();
			if (nextMatch.didFindMatch) {
				newPosition = nextMatch.newPosition;
				oldPosition = nextMatch.oldPosition;
			} else {
				newPosition = (int) newData.length();
			}

			// Extend the current match (|newPosition|, |oldPosition|) backward such that
			// 50% of the bytes
			// match. We have written diff / extra data up till |lastNewPosition| so we
			// cannot extend
			// further back than |lastNewPosition|.
			int backwardExtension = 0;
			if (newPosition < newData.length()) {
				int score = 0;
				int bestScore = 0;
				for (int i = 1; newPosition - i >= lastNewPosition && oldPosition >= i; ++i) {
					oldData.seek(oldPosition - i);
					newData.seek(newPosition - i);
					if (oldData.readByte() == newData.readByte()) {
						++score;
					} else {
						--score;
					}

					if (score > bestScore) {
						bestScore = score;
						backwardExtension = i;
					}
				}
			}

			// Extend the previous match (|lastNewPosition|, |lastOldPosition|) forward such
			// that 50% of
			// the bytes match. (|lastNewPosition|, |lastOldPosition|) were extended
			// backward in the
			// previous iteration of the loop.
			int forwardExtension = 0;
			{
				int score = 0;
				int bestScore = 0;
				oldData.seek(lastOldPosition);
				newData.seek(lastNewPosition);
				for (int i = 0; lastNewPosition + i < newPosition && lastOldPosition + i < oldData.length(); ++i) {
					if (oldData.readByte() == newData.readByte()) {
						++score;
					} else {
						--score;
					}
					if (score > bestScore) {
						bestScore = score;
						forwardExtension = i + 1;
					}
				}
			}

			// Adjust |backwardExtension| and |forwardExtension| such that the extended
			// matches do
			// not intersect in |newData|. They can intersect in |oldData|.
			int overlap = (lastNewPosition + forwardExtension) - (newPosition - backwardExtension);
			if (overlap > 0) {
				int score = 0;
				int bestScore = 0;
				int backwardExtensionDecrement = 0;
				for (int i = 0; i < overlap; ++i) {
					newData.seek(lastNewPosition + forwardExtension - overlap + i);
					oldData.seek(lastOldPosition + forwardExtension - overlap + i);
					if (newData.readByte() == oldData.readByte()) {
						++score;
					}

					newData.seek(newPosition - backwardExtension + i);
					oldData.seek(oldPosition - backwardExtension + i);
					if (newData.readByte() == oldData.readByte()) {
						--score;
					}
					if (score > bestScore) {
						bestScore = score;
						backwardExtensionDecrement = i + 1;
					}
				}
				forwardExtension -= overlap - backwardExtensionDecrement;
				backwardExtension -= backwardExtensionDecrement;
			}

			// Write an entry with:
			// - The diff between |newData| and |oldData| for the previous extended match:
			// oldData[lastOldPosition ... lastOldPosition + forwardExtension - 1] and
			// newData[lastNewPosition ... lastNewPosition + forwardExtension - 1].
			// - The bytes in |newData| between |lastNewPosition| and |newPosition| which
			// are part of
			// neither the previous extended match or the new extended match:
			// newData[lastNewPosition + forwardExtension ... newPosition -
			// backwardExtension - 1]

			int oldPositionOffset = 0;
			if (newPosition < newData.length()) {
				// The offset from the byte after the last byte of the previous match in
				// |newData| to the
				// first byte of the new match in |oldData|.
				oldPositionOffset = (oldPosition - backwardExtension) - (lastOldPosition + forwardExtension);
			}

			// The number of bytes in |newData| between |lastNewPosition| and |newPosition|
			// which are part
			// of neither the previous extended match or the new extended match.
			int newNoMatchLength = (newPosition - backwardExtension) - (lastNewPosition + forwardExtension);

			writeEntry(newData, oldData, lastNewPosition, lastOldPosition, forwardExtension, newNoMatchLength,
					oldPositionOffset, outputStream);

			lastNewPosition = newPosition - backwardExtension;
			lastOldPosition = oldPosition - backwardExtension;
		}
	}

	/**
	 * Generate a diff between the old data and the new, writing to the specified
	 * stream. Uses {@link #DEFAULT_MINIMUM_MATCH_LENGTH} as the match length.
	 *
	 * @param oldData                   the old data
	 * @param newData                   the new data
	 * @param outputStream              where output should be written
	 * @param randomAccessObjectFactory factory to create auxiliary storage during
	 *                                  BsDiff
	 * @throws IOException          if unable to read or write data
	 * @throws InterruptedException if any thread interrupts this thread
	 */
	public static void generatePatch(final RandomAccessObject oldData, final RandomAccessObject newData,
			final OutputStream outputStream, final RandomAccessObjectFactory randomAccessObjectFactory)
			throws IOException, InterruptedException {
		generatePatch(oldData, newData, outputStream, randomAccessObjectFactory, DEFAULT_MINIMUM_MATCH_LENGTH);
	}

	/**
	 * Generate a diff between the old data and the new, writing to the specified
	 * stream. Uses in-memory byte array storage for ancillary allocations and
	 * {@link #DEFAULT_MINIMUM_MATCH_LENGTH} as the match length.
	 *
	 * @param oldData      the old data
	 * @param newData      the new data
	 * @param outputStream where output should be written
	 * @throws IOException          if unable to read or write data
	 * @throws InterruptedException if any thread interrupts this thread
	 */
	public static void generatePatch(final byte[] oldData, final byte[] newData, final OutputStream outputStream)
			throws IOException, InterruptedException {
		generatePatch(oldData, newData, outputStream, DEFAULT_MINIMUM_MATCH_LENGTH);
	}

	/**
	 * Generate a diff between the old data and the new, writing to the specified
	 * stream. Uses in-memory byte array storage for ancillary allocations.
	 *
	 * @param oldData            the old data
	 * @param newData            the new data
	 * @param outputStream       where output should be written
	 * @param minimumMatchLength the minimum "match" (in bytes) for BsDiff to
	 *                           consider between the oldData and newData. This can
	 *                           have a significant effect on both the generated
	 *                           patch size and the amount of time and memory
	 *                           required to apply the patch.
	 * @throws IOException          if unable to read or write data
	 * @throws InterruptedException if any thread interrupts this thread
	 */
	public static void generatePatch(final byte[] oldData, final byte[] newData, final OutputStream outputStream,
			final int minimumMatchLength) throws IOException, InterruptedException {
		try (RandomAccessObject oldDataRAO = new RandomAccessObject.RandomAccessByteArrayObject(oldData);
				RandomAccessObject newDataRAO = new RandomAccessObject.RandomAccessByteArrayObject(newData);) {
			generatePatch(oldDataRAO, newDataRAO, outputStream,
					new RandomAccessObjectFactory.RandomAccessByteArrayObjectFactory(), minimumMatchLength);
		}
	}

	/**
	 * Generate a diff between the old data and the new, writing to the specified
	 * stream. Uses file-based storage for ancillary operations and
	 * {@link #DEFAULT_MINIMUM_MATCH_LENGTH} as the match length.
	 *
	 * @param oldData      a file containing the old data
	 * @param newData      a file containing the new data
	 * @param outputStream where output should be written
	 * @throws IOException          if unable to read or write data
	 * @throws InterruptedException if any thread interrupts this thread
	 */
	public static void generatePatch(final File oldData, final File newData, final OutputStream outputStream)
			throws IOException, InterruptedException {
		generatePatch(oldData, newData, outputStream, DEFAULT_MINIMUM_MATCH_LENGTH);
	}

	/**
	 * Generate a diff between the old data and the new, writing to the specified
	 * stream. Uses file-based storage for ancillary allocations.
	 *
	 * @param oldData            a file containing the old data
	 * @param newData            a file containing the new data
	 * @param outputStream       where output should be written
	 * @param minimumMatchLength the minimum "match" (in bytes) for BsDiff to
	 *                           consider between the oldData and newData. This can
	 *                           have a significant effect on both the generated
	 *                           patch size and the amount of time and memory
	 *                           required to apply the patch.
	 * @throws IOException          if unable to read or write data
	 * @throws InterruptedException if any thread interrupts this thread
	 */
	public static void generatePatch(final File oldData, final File newData, final OutputStream outputStream,
			final int minimumMatchLength) throws IOException, InterruptedException {
		try (RandomAccessFile oldDataRAF = new RandomAccessFile(oldData, "r");
				RandomAccessFile newDataRAF = new RandomAccessFile(newData, "r");
				RandomAccessObject oldDataRAO = new RandomAccessObject.RandomAccessMmapObject(oldDataRAF, "r");
				RandomAccessObject newDataRAO = new RandomAccessObject.RandomAccessMmapObject(newDataRAF, "r");) {
			generatePatch(oldDataRAO, newDataRAO, outputStream,
					new RandomAccessObjectFactory.RandomAccessMmapObjectFactory("rw"), minimumMatchLength);
		}

		// Due to a bug in the JVM (http://bugs.java.com/view_bug.do?bug_id=6417205), we
		// need to call
		// gc() and runFinalization() explicitly to get rid of any MappedByteBuffers we
		// may have used
		// during patch generation.
		System.gc();
		System.runFinalization();
	}

	/**
	 * Generate a diff between the old data and the new, writing to the specified
	 * stream.
	 *
	 * @param oldData                   the old data
	 * @param newData                   the new data
	 * @param outputStream              where output should be written
	 * @param randomAccessObjectFactory factory to create auxiliary storage during
	 *                                  BsDiff
	 * @param minimumMatchLength        the minimum "match" (in bytes) for BsDiff to
	 *                                  consider between the oldData and newData.
	 *                                  This can have a significant effect on both
	 *                                  the generated patch size and the amount of
	 *                                  time and memory required to apply the patch.
	 * @throws IOException          if unable to read or write data
	 * @throws InterruptedException if any thread interrupts this thread
	 */
	public static void generatePatch(final RandomAccessObject oldData, final RandomAccessObject newData,
			final OutputStream outputStream, final RandomAccessObjectFactory randomAccessObjectFactory,
			final int minimumMatchLength) throws IOException, InterruptedException {
		// Write header (signature + new file length)
		outputStream.write("ENDSLEY/BSDIFF43".getBytes(StandardCharsets.US_ASCII));
		BsUtil.writeFormattedLong(newData.length(), outputStream);

		// Do the suffix search.
		try (final RandomAccessObject groupArray = new DivSuffixSorter(randomAccessObjectFactory).suffixSort(oldData)) {
			BsDiffMatcher matcher = new BsDiffMatcher(oldData, newData, groupArray, minimumMatchLength);
			generatePatchWithMatcher(oldData, newData, matcher, outputStream);
		}
	}
}
