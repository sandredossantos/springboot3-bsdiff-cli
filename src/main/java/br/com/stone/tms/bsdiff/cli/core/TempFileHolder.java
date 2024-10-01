package br.com.stone.tms.bsdiff.cli.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class TempFileHolder implements Closeable {

	public final File file;

	public TempFileHolder() throws IOException {
		file = File.createTempFile("archive_patcher", "tmp");
		file.deleteOnExit();
	}

	@Override
	public void close() throws IOException {
		file.delete();
	}
}
