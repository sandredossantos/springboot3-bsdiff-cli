package br.com.stone.tms.bsdiff.cli.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

public class DefaultDeflateCompatibilityWindow {

	private static final class BaselineHolder {
		private static final Map<JreDeflateParameters, String> BASELINE_INSTANCE = generateBaseline();
	}

	private static final Map<JreDeflateParameters, String> generateBaseline() {
		Map<JreDeflateParameters, String> baseline = new HashMap<JreDeflateParameters, String>();
		baseline.put(JreDeflateParameters.of(1, 0, true),
				"5e0ae60766a04b0c9ef1f677ae4ba4a83a6bc112ce3761b41b270af08821804e");
		baseline.put(JreDeflateParameters.of(2, 0, true),
				"9b392414e62afcc64200cc39955ff75d1254f56c67bf2eb05d62f63b677080fc");
		baseline.put(JreDeflateParameters.of(3, 0, true),
				"ce272e7f72232e80b5d00d7333a5bdd6e9d7e34268d49c5fe9bdfedba6fc0d54");
		baseline.put(JreDeflateParameters.of(4, 0, true),
				"a8a3b59d42fe257766926d46818422216a043c8c37bb69492d9bab3bd4d6b07a");
		baseline.put(JreDeflateParameters.of(5, 0, true),
				"49280186dd6683ae92ef25e239d7c0e2b7a4fd0e2b7dfadc8846f5157aa6aed9");
		baseline.put(JreDeflateParameters.of(6, 0, true),
				"bec508de691537047e338825828db16308cc8dc93e22386c8eeb0bc14c4c5f45");
		baseline.put(JreDeflateParameters.of(7, 0, true),
				"6daf3724aed1f67c7d1f6404166b5dbea1f2fc42192f20813910271bc8c40e75");
		baseline.put(JreDeflateParameters.of(8, 0, true),
				"08cd258637bb146d33ef550fc60baaa855902837758d6489802f3b1ece6ea7f1");
		baseline.put(JreDeflateParameters.of(9, 0, true),
				"5ea67964bb124b436130dfbbd2e36fb2b08992423be188a8edfbb8550e8bfefb");
		baseline.put(JreDeflateParameters.of(1, 1, true),
				"5e0ae60766a04b0c9ef1f677ae4ba4a83a6bc112ce3761b41b270af08821804e");
		baseline.put(JreDeflateParameters.of(2, 1, true),
				"9b392414e62afcc64200cc39955ff75d1254f56c67bf2eb05d62f63b677080fc");
		baseline.put(JreDeflateParameters.of(3, 1, true),
				"ce272e7f72232e80b5d00d7333a5bdd6e9d7e34268d49c5fe9bdfedba6fc0d54");
		baseline.put(JreDeflateParameters.of(4, 1, true),
				"6283bb35a97f4657b6aab0b0a7f218947965f135838926df295037fdca816746");
		baseline.put(JreDeflateParameters.of(5, 1, true),
				"42594bbcf7fa83f74cdf35839debaae25e4655070fdf1fc67539de0a90f59afe");
		baseline.put(JreDeflateParameters.of(6, 1, true),
				"1db82cae52b0bb88cf3a21cdec183c1dab8074b1d1f4341b9e9b18b1ace5a778");
		baseline.put(JreDeflateParameters.of(7, 1, true),
				"5d0d53667944dc447b52e58b0e91e303b5662f92a085ab5a1f4b62eeab8900ef");
		baseline.put(JreDeflateParameters.of(8, 1, true),
				"c6cdfbe16b1e530e91fd3ac1dbb2a9b2f5b3ccee5ddf92769ea349fc60fd560e");
		baseline.put(JreDeflateParameters.of(9, 1, true),
				"f4e93a15b50c568d39785c12d373104272009bcd71028dbf0faa85441eb5130d");
		baseline.put(JreDeflateParameters.of(1, 2, true),
				"2297dbc0a5498c9a7a89519f401936e910ddb82c9b477e7aa407a4c2bf523dbd");
		baseline.put(JreDeflateParameters.of(1, 0, false),
				"5e06d9c9280e5b9b4832c0894e2f930f606665169ad2ac093df544e70fac4136");
		baseline.put(JreDeflateParameters.of(2, 0, false),
				"f1c2fe9b4189c03a5ae0b1a1db51875d334fb21144e08e9c527644d66ef39797");
		baseline.put(JreDeflateParameters.of(3, 0, false),
				"49998ee364d2668eb5a2cadf40feaa78c0c081337141ad15f7fb2a7843c833b8");
		baseline.put(JreDeflateParameters.of(4, 0, false),
				"6911a5b04664b00b2bba72d7ba9e1d5a73b390f2cf4b20618580c13a5825fc17");
		baseline.put(JreDeflateParameters.of(5, 0, false),
				"417f5fd21438ffb739a681af9a20eed29dd9da63e8a540415b9ec6199495e6db");
		baseline.put(JreDeflateParameters.of(6, 0, false),
				"9a4bcc9afd8547784aff6283cafd69f46893d5131bd798fbad92dc52ca946522");
		baseline.put(JreDeflateParameters.of(7, 0, false),
				"592ad846a99693b2f1092bac6a3bf2cf5ac562a9b38ebe34c46cbf2ddd3c13aa");
		baseline.put(JreDeflateParameters.of(8, 0, false),
				"8d4b91929384dfd7a0dda6b6e0410de7c4c109167047d694cf36b46e68dd8d5f");
		baseline.put(JreDeflateParameters.of(9, 0, false),
				"36bacacc32707e6498269a04d2b2cd30990ac4b0717ee4a9e4badbb6ca5fb7ea");
		baseline.put(JreDeflateParameters.of(1, 1, false),
				"5e06d9c9280e5b9b4832c0894e2f930f606665169ad2ac093df544e70fac4136");
		baseline.put(JreDeflateParameters.of(2, 1, false),
				"f1c2fe9b4189c03a5ae0b1a1db51875d334fb21144e08e9c527644d66ef39797");
		baseline.put(JreDeflateParameters.of(3, 1, false),
				"49998ee364d2668eb5a2cadf40feaa78c0c081337141ad15f7fb2a7843c833b8");
		baseline.put(JreDeflateParameters.of(4, 1, false),
				"2bd9ae26fe933102ed46ef2bf8e82d62e0104d9d1cce73a8b46df8a238fd32f8");
		baseline.put(JreDeflateParameters.of(5, 1, false),
				"6410581a92808f97f695e796c2963cb6e111af1ec7b7e7d155dcb601192dd80a");
		baseline.put(JreDeflateParameters.of(6, 1, false),
				"50571149806edb22b7f3a3ba52168644dd99de444e813df7e186817ccc204c01");
		baseline.put(JreDeflateParameters.of(7, 1, false),
				"7a41b9549bcc651d3d219e7aaf3f74beefea238caf1560036cd299d62be6531b");
		baseline.put(JreDeflateParameters.of(8, 1, false),
				"29da81b218ff50e69819375d2c008a648309dd9a0fc18683d675ce523cff744f");
		baseline.put(JreDeflateParameters.of(9, 1, false),
				"4ce8c7903e526e2a36db168c5cf9af0b90155850899ea26ad77d6daaa7b395c3");
		baseline.put(JreDeflateParameters.of(1, 2, false),
				"e3cc7200f308fa7756f02bebbf5046e58a4a2a7e8f1c9ea1708b96d4e1033666");
		return Collections.unmodifiableMap(baseline);
	}

	public Map<JreDeflateParameters, String> getBaselineValues() {
		return BaselineHolder.BASELINE_INSTANCE;
	}

	private static final String CORPUS_BASE_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt "
			+ "ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation "
			+ "ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in "
			+ "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
			+ "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt "
			+ "mollit anim id est laborum.";

	private static final class CorpusHolder {
		private static final byte[] CORPUS_INSTANCE = generateCorpus();
	}

	private static final byte[] generateCorpus() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final byte[] loremIpsumBytes;
		try {
			loremIpsumBytes = CORPUS_BASE_TEXT.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("System doesn't support ASCII", e);
		}
		// This is sufficient to create different results for all 9 compression
		// levels of the default strategy by exercising the hash chaining
		// longest-match logic in zlib. The data totals about 9k.
		for (int x = 0; x < 135; x++) {
			buffer.write(loremIpsumBytes, 0, x);
		}
		return buffer.toByteArray();
	}

	public byte[] getCorpus() {
		return CorpusHolder.CORPUS_INSTANCE.clone();
	}

	private final static String hexString(byte[] bytes) {
		StringBuilder buffer = new StringBuilder();
		for (int x = 0; x < bytes.length; x++) {
			int value = bytes[x] & 0xff;
			if (value < 0x10) {
				buffer.append('0');
			}
			buffer.append(Integer.toHexString(value));
		}
		return buffer.toString();
	}

	public boolean isCompatible() {
		return getIncompatibleValues().isEmpty();
	}

	public Map<JreDeflateParameters, String> getIncompatibleValues() {
		Map<JreDeflateParameters, String> incompatible = new HashMap<JreDeflateParameters, String>();
		Map<JreDeflateParameters, String> systemValues = getSystemValues();
		for (Map.Entry<JreDeflateParameters, String> baselineEntry : getBaselineValues().entrySet()) {
			String computedSHA256 = systemValues.get(baselineEntry.getKey());
			if (!computedSHA256.equals(baselineEntry.getValue())) {
				incompatible.put(baselineEntry.getKey(), computedSHA256);
			}
		}
		return incompatible;
	}

	public Map<JreDeflateParameters, String> getSystemValues() {
		Map<JreDeflateParameters, String> result = new HashMap<JreDeflateParameters, String>();
		MessageDigest digester;
		try {
			digester = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("System doesn't support SHA-256", e);
		}

		DeflateCompressor compressor = new DeflateCompressor();
		compressor.setCaching(true); // Makes this computation lighter weight.
		boolean[] nowrapValues = { true, false };
		int[] strategies = { Deflater.DEFAULT_STRATEGY, Deflater.FILTERED, Deflater.HUFFMAN_ONLY };
		int[] levels = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		for (final boolean nowrap : nowrapValues) {
			compressor.setNowrap(nowrap);
			for (final int strategy : strategies) {
				compressor.setStrategy(strategy);
				final int[] relevantLevels;
				if (strategy == Deflater.HUFFMAN_ONLY) {
					// There is no concept of a compression level with this
					// strategy.
					relevantLevels = new int[] { 1 };
				} else {
					relevantLevels = levels;
				}
				for (final int level : relevantLevels) {
					compressor.setCompressionLevel(level);
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					try {
						compressor.compress(new ByteArrayInputStream(CorpusHolder.CORPUS_INSTANCE), buffer);
					} catch (IOException e) {
						throw new RuntimeException(e); // This should never occur as it's all in-memory.
					}
					byte[] compressedData = buffer.toByteArray();
					digester.reset();
					byte[] sha256OfCompressedData = digester.digest(compressedData);
					String sha256String = hexString(sha256OfCompressedData);
					JreDeflateParameters parameters = JreDeflateParameters.of(level, strategy, nowrap);
					result.put(parameters, sha256String);
				}
			}
		}
		compressor.release();
		return result;
	}
}
