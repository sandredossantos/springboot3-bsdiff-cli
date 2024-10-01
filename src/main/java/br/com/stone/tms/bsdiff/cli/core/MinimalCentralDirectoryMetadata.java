package br.com.stone.tms.bsdiff.cli.core;

class MinimalCentralDirectoryMetadata {
	/**
	 * The number of entries in the central directory.
	 */
	private final int numEntriesInCentralDirectory;

	/**
	 * The file offset of the first byte of the central directory.
	 */
	private final long offsetOfCentralDirectory;

	/**
	 * The length of the central directory, in bytes.
	 */
	private final long lengthOfCentralDirectory;

	/**
	 * Constructs a new metadata object with the specified values
	 * 
	 * @param numEntriesInCentralDirectory the number of entries in the central
	 *                                     directory
	 * @param offsetOfCentralDirectory     the file offset of the first byte of the
	 *                                     central directory
	 * @param lengthOfCentralDirectory     the length of the central directory, in
	 *                                     bytes
	 */
	MinimalCentralDirectoryMetadata(int numEntriesInCentralDirectory, long offsetOfCentralDirectory,
			long lengthOfCentralDirectory) {
		this.numEntriesInCentralDirectory = numEntriesInCentralDirectory;
		this.offsetOfCentralDirectory = offsetOfCentralDirectory;
		this.lengthOfCentralDirectory = lengthOfCentralDirectory;
	}

	/**
	 * Returns the number of entries in the central directory.
	 * 
	 * @return as described
	 */
	public final int getNumEntriesInCentralDirectory() {
		return numEntriesInCentralDirectory;
	}

	/**
	 * Returns the file offset of the first byte of the central directory.
	 * 
	 * @return as described
	 */
	public final long getOffsetOfCentralDirectory() {
		return offsetOfCentralDirectory;
	}

	/**
	 * Returns the length of the central directory, in bytes.
	 * 
	 * @return as described
	 */
	public final long getLengthOfCentralDirectory() {
		return lengthOfCentralDirectory;
	}
}
