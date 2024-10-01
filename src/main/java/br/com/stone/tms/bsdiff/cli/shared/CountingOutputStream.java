package br.com.stone.tms.bsdiff.cli.shared;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CountingOutputStream extends FilterOutputStream {

	private long bytesWritten = 0;

	public CountingOutputStream(OutputStream out) {
		super(out);
	}

	public long getNumBytesWritten() {
		return bytesWritten;
	}

	@Override
	public void write(int b) throws IOException {
		bytesWritten++;
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		bytesWritten += b.length;
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		bytesWritten += len;
		out.write(b, off, len);
	}
}
