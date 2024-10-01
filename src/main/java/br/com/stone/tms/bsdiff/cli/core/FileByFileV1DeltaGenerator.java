package br.com.stone.tms.bsdiff.cli.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.com.stone.tms.bsdiff.cli.core.bsdiff.BsDiffDeltaGenerator;

public class FileByFileV1DeltaGenerator implements DeltaGenerator {

	private final List<RecommendationModifier> recommendationModifiers;

	public FileByFileV1DeltaGenerator(RecommendationModifier... recommendationModifiers) {
		if (recommendationModifiers != null) {
			this.recommendationModifiers = Collections.unmodifiableList(Arrays.asList(recommendationModifiers));
		} else {
			this.recommendationModifiers = Collections.emptyList();
		}
	}

	@Override
	public void generateDelta(File oldFile, File newFile, OutputStream patchOut)
			throws IOException, InterruptedException {
		try (TempFileHolder deltaFriendlyOldFile = new TempFileHolder();
				TempFileHolder deltaFriendlyNewFile = new TempFileHolder();
				TempFileHolder deltaFile = new TempFileHolder();
				FileOutputStream deltaFileOut = new FileOutputStream(deltaFile.file);
				BufferedOutputStream bufferedDeltaOut = new BufferedOutputStream(deltaFileOut)) {
			PreDiffExecutor.Builder builder = new PreDiffExecutor.Builder().readingOriginalFiles(oldFile, newFile)
					.writingDeltaFriendlyFiles(deltaFriendlyOldFile.file, deltaFriendlyNewFile.file);
			for (RecommendationModifier modifier : recommendationModifiers) {
				builder.withRecommendationModifier(modifier);
			}
			PreDiffExecutor executor = builder.build();
			PreDiffPlan preDiffPlan = executor.prepareForDiffing();
			DeltaGenerator deltaGenerator = getDeltaGenerator();
			deltaGenerator.generateDelta(deltaFriendlyOldFile.file, deltaFriendlyNewFile.file, bufferedDeltaOut);
			bufferedDeltaOut.close();
			PatchWriter patchWriter = new PatchWriter(preDiffPlan, deltaFriendlyOldFile.file.length(),
					deltaFriendlyNewFile.file.length(), deltaFile.file);
			patchWriter.writeV1Patch(patchOut);
		}
	}

	public PreDiffPlan generatePreDiffPlan(File oldFile, File newFile) throws IOException, InterruptedException {
		try (TempFileHolder deltaFriendlyOldFile = new TempFileHolder();
				TempFileHolder deltaFriendlyNewFile = new TempFileHolder()) {
			PreDiffExecutor.Builder builder = new PreDiffExecutor.Builder().readingOriginalFiles(oldFile, newFile)
					.writingDeltaFriendlyFiles(deltaFriendlyOldFile.file, deltaFriendlyNewFile.file);
			for (RecommendationModifier modifier : recommendationModifiers) {
				builder.withRecommendationModifier(modifier);
			}

			PreDiffExecutor executor = builder.build();

			return executor.prepareForDiffing();
		}
	}

	protected DeltaGenerator getDeltaGenerator() {
		return new BsDiffDeltaGenerator();
	}
}
