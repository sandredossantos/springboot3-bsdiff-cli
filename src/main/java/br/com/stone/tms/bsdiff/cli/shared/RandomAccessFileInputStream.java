package br.com.stone.tms.bsdiff.cli.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileInputStream extends InputStream {

	private final RandomAccessFile raf;
	private long mark = -1;
	private long rangeOffset;
	private long rangeLength;
	private final long fileLength;

	public RandomAccessFileInputStream(File file) throws IOException {
		this(file, 0, file.length());
	}

	public RandomAccessFileInputStream(File file, long rangeOffset, long rangeLength) throws IOException {
		raf = getRandomAccessFile(file);
		fileLength = file.length();
		setRange(rangeOffset, rangeLength);
	}

	protected RandomAccessFile getRandomAccessFile(File file) throws IOException {
		return new RandomAccessFile(file, "r");
	}

	public void setRange(long rangeOffset, long rangeLength) throws IOException {
		if (rangeOffset < 0) {
			throw new IllegalArgumentException("rangeOffset must be >= 0");
		}
		if (rangeLength < 0) {
			throw new IllegalArgumentException("rangeLength must be >= 0");
		}
		if (rangeOffset + rangeLength > fileLength) {
			throw new IllegalArgumentException("Read range exceeds file length");
		}
		if (rangeOffset + rangeLength < 0) {
			throw new IllegalArgumentException("Insane input size not supported");
		}
		this.rangeOffset = rangeOffset;
		this.rangeLength = rangeLength;
		mark = rangeOffset;
		reset();
		mark = -1;
	}

	@Override
	public int available() throws IOException {
		long rangeRelativePosition = raf.getFilePointer() - rangeOffset;
		long result = rangeLength - rangeRelativePosition;
		if (result > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) result;
	}

	public long getPosition() throws IOException {
		return raf.getFilePointer();
	}

	@Override
	public void close() throws IOException {
		raf.close();
	}

	@Override
	public int read() throws IOException {
		if (available() <= 0) {
			return -1;
		}
		return raf.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len <= 0) {
			return 0;
		}
		int available = available();
		if (available <= 0) {
			return -1;
		}
		int result = raf.read(b, off, Math.min(len, available));
		return result;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public long skip(long n) throws IOException {
		if (n <= 0) {
			return 0;
		}
		int available = available();
		if (available <= 0) {
			return 0;
		}
		int skipAmount = (int) Math.min(available, n);
		raf.seek(raf.getFilePointer() + skipAmount);
		return skipAmount;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readlimit) {
		try {
			mark = raf.getFilePointer();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void reset() throws IOException {
		if (mark < 0) {
			throw new IOException("mark not set");
		}
		raf.seek(mark);
	}

	public long length() {
		return fileLength;
	}
}
