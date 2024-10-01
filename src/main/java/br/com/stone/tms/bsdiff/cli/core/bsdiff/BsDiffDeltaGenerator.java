package br.com.stone.tms.bsdiff.cli.core.bsdiff;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import br.com.stone.tms.bsdiff.cli.core.DeltaGenerator;

public class BsDiffDeltaGenerator implements DeltaGenerator {

	private static final int MATCH_LENGTH_BYTES = 16;

	@Override
	public void generateDelta(File oldBlob, File newBlob, OutputStream deltaOut)
			throws IOException, InterruptedException {
		BsDiffPatchWriter.generatePatch(oldBlob, newBlob, deltaOut, MATCH_LENGTH_BYTES);
	}
}
