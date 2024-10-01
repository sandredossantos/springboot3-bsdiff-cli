package br.com.stone.tms.bsdiff.cli.shared;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ByteArrayInputStreamFactory implements MultiViewInputStreamFactory {

	private final byte[] bytes;

	public ByteArrayInputStreamFactory(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public ByteArrayInputStream newStream() throws IOException {
		return new ByteArrayInputStream(bytes);
	}
}
