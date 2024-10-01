package br.com.stone.tms.bsdiff.cli.core;

public enum Recommendation {
	UNCOMPRESS_OLD(true, false), UNCOMPRESS_NEW(false, true), UNCOMPRESS_BOTH(true, true),
	UNCOMPRESS_NEITHER(false, false);

	public final boolean uncompressOldEntry;
	public final boolean uncompressNewEntry;

	private Recommendation(boolean uncompressOldEntry, boolean uncompressNewEntry) {
		this.uncompressOldEntry = uncompressOldEntry;
		this.uncompressNewEntry = uncompressNewEntry;
	}
}
