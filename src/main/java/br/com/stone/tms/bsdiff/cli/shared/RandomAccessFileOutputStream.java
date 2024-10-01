package br.com.stone.tms.bsdiff.cli.shared;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileOutputStream extends OutputStream {

	private final RandomAccessFile raf;

	public RandomAccessFileOutputStream(File outputFile, long expectedSize) throws IOException {
		this.raf = getRandomAccessFile(outputFile);
		if (expectedSize >= 0) {
			raf.setLength(expectedSize);
			if (raf.length() != expectedSize) {
				throw new IOException("Unable to set the file size");
			}
		}
	}

	protected RandomAccessFile getRandomAccessFile(File file) throws IOException {
		return new RandomAccessFile(file, "rw");
	}

	@Override
	public void write(int b) throws IOException {
		raf.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		raf.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		raf.getChannel().force(true);
	}

	@Override
	public void close() throws IOException {
		flush();
		raf.close();
	}
}
