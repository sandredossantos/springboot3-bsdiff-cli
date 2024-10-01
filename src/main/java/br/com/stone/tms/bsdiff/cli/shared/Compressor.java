package br.com.stone.tms.bsdiff.cli.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Compressor {
	public void compress(InputStream uncompressedIn, OutputStream compressedOut) throws IOException;
}
