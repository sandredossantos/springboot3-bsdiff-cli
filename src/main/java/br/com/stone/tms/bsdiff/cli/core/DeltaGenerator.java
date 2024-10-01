package br.com.stone.tms.bsdiff.cli.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface DeltaGenerator {
	public void generateDelta(File oldBlob, File newBlob, OutputStream deltaOut)
			throws IOException, InterruptedException;
}
