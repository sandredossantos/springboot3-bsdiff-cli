package br.com.stone.tms.bsdiff.cli.shared;

import java.io.File;
import java.io.IOException;

public class RandomAccessFileInputStreamFactory implements MultiViewInputStreamFactory {

	private final File file;
	private final long rangeOffset;
	private final long rangeLength;

	public RandomAccessFileInputStreamFactory(File file, long rangeOffset, long rangeLength) {
		this.file = file;
		this.rangeOffset = rangeOffset;
		this.rangeLength = rangeLength;
	}

	@Override
	public RandomAccessFileInputStream newStream() throws IOException {
		return new RandomAccessFileInputStream(file, rangeOffset, rangeLength);
	}
}
