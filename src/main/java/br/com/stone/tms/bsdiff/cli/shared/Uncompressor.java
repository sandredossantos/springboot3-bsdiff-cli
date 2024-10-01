package br.com.stone.tms.bsdiff.cli.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Uncompressor {
	public void uncompress(InputStream compressedIn, OutputStream uncompressedOut) throws IOException;
}
