package br.com.stone.tms.bsdiff.cli.core;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import br.com.stone.tms.bsdiff.cli.shared.JreDeflateParameters;
import br.com.stone.tms.bsdiff.cli.shared.PatchConstants;
import br.com.stone.tms.bsdiff.cli.shared.TypedRange;

public class PatchWriter {
	/**
	 * The patch plan.
	 */
	private final PreDiffPlan plan;

	/**
	 * The expected size of the delta-friendly old file, provided as a convenience
	 * for the patch <strong>applier</strong> to reserve space on the filesystem for
	 * applying the patch.
	 */
	private final long deltaFriendlyOldFileSize;

	/**
	 * The expected size of the delta-friendly new file, provided for forward
	 * compatibility.
	 */
	private final long deltaFriendlyNewFileSize;

	/**
	 * The delta that transforms the old delta-friendly file into the new
	 * delta-friendly file.
	 */
	private final File deltaFile;

	/**
	 * Creates a new patch writer.
	 *
	 * @param plan                     the patch plan
	 * @param deltaFriendlyOldFileSize the expected size of the delta-friendly old
	 *                                 file, provided as a convenience for the patch
	 *                                 <strong>applier</strong> to reserve space on
	 *                                 the filesystem for applying the patch
	 * @param deltaFriendlyNewFileSize the expected size of the delta-friendly new
	 *                                 file, provided for forward compatibility
	 * @param deltaFile                the delta that transforms the old
	 *                                 delta-friendly file into the new
	 *                                 delta-friendly file
	 */
	public PatchWriter(PreDiffPlan plan, long deltaFriendlyOldFileSize, long deltaFriendlyNewFileSize, File deltaFile) {
		this.plan = plan;
		this.deltaFriendlyOldFileSize = deltaFriendlyOldFileSize;
		this.deltaFriendlyNewFileSize = deltaFriendlyNewFileSize;
		this.deltaFile = deltaFile;
	}

	/**
	 * Write a v1-style patch to the specified output stream.
	 * 
	 * @param out the stream to write the patch to
	 * @throws IOException if anything goes wrong
	 */
	public void writeV1Patch(OutputStream out) throws IOException {
		// Use DataOutputStream for ease of writing. This is deliberately left open, as
		// closing it would
		// close the output stream that was passed in and that is not part of the
		// method's documented
		// behavior.
		@SuppressWarnings("resource")
		DataOutputStream dataOut = new DataOutputStream(out);

		dataOut.write(PatchConstants.IDENTIFIER.getBytes("US-ASCII"));
		dataOut.writeInt(0); // Flags (reserved)
		dataOut.writeLong(deltaFriendlyOldFileSize);

		// Write out all the delta-friendly old file uncompression instructions
		dataOut.writeInt(plan.getOldFileUncompressionPlan().size());
		for (TypedRange<Void> range : plan.getOldFileUncompressionPlan()) {
			dataOut.writeLong(range.getOffset());
			dataOut.writeLong(range.getLength());
		}

		// Write out all the delta-friendly new file recompression instructions
		dataOut.writeInt(plan.getDeltaFriendlyNewFileRecompressionPlan().size());
		for (TypedRange<JreDeflateParameters> range : plan.getDeltaFriendlyNewFileRecompressionPlan()) {
			dataOut.writeLong(range.getOffset());
			dataOut.writeLong(range.getLength());
			// Write the deflate information
			dataOut.write(PatchConstants.CompatibilityWindowId.DEFAULT_DEFLATE.patchValue);
			dataOut.write(range.getMetadata().level);
			dataOut.write(range.getMetadata().strategy);
			dataOut.write(range.getMetadata().nowrap ? 1 : 0);
		}

		// Now the delta section
		// First write the number of deltas present in the patch. In v1, there is always
		// exactly one
		// delta, and it is for the entire input; in future versions there may be
		// multiple deltas, of
		// arbitrary types.
		dataOut.writeInt(1);
		// In v1 the delta format is always bsdiff, so write it unconditionally.
		dataOut.write(PatchConstants.DeltaFormat.BSDIFF.patchValue);

		// Write the working ranges. In v1 these are always the entire contents of the
		// delta-friendly
		// old file and the delta-friendly new file. These are for forward compatibility
		// with future
		// versions that may allow deltas of arbitrary formats to be mapped to arbitrary
		// ranges.
		dataOut.writeLong(0); // i.e., start of the working range in the delta-friendly old file
		dataOut.writeLong(deltaFriendlyOldFileSize); // i.e., length of the working range in old
		dataOut.writeLong(0); // i.e., start of the working range in the delta-friendly new file
		dataOut.writeLong(deltaFriendlyNewFileSize); // i.e., length of the working range in new

		// Finally, the length of the delta and the delta itself.
		dataOut.writeLong(deltaFile.length());
		try (FileInputStream deltaFileIn = new FileInputStream(deltaFile);
				BufferedInputStream deltaIn = new BufferedInputStream(deltaFileIn)) {
			byte[] buffer = new byte[32768];
			int numRead = 0;
			while ((numRead = deltaIn.read(buffer)) >= 0) {
				dataOut.write(buffer, 0, numRead);
			}
		}
		dataOut.flush();
	}
}