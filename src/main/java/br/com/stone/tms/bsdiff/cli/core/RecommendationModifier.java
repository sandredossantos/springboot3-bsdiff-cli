package br.com.stone.tms.bsdiff.cli.core;

import java.io.File;
import java.util.List;

public interface RecommendationModifier {
	public List<QualifiedRecommendation> getModifiedRecommendations(File oldFile, File newFile,
			List<QualifiedRecommendation> originalRecommendations);
}
