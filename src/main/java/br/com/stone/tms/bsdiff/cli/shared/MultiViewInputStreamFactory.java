package br.com.stone.tms.bsdiff.cli.shared;

import java.io.IOException;
import java.io.InputStream;

public interface MultiViewInputStreamFactory {

	public InputStream newStream() throws IOException;
}
