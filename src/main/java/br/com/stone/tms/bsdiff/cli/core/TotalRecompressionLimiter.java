package br.com.stone.tms.bsdiff.cli.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TotalRecompressionLimiter implements RecommendationModifier {

	@Override
	public List<QualifiedRecommendation> getModifiedRecommendations(File oldFile, File newFile,
			List<QualifiedRecommendation> originalRecommendations) {

		List<QualifiedRecommendation> sorted = new ArrayList<QualifiedRecommendation>(originalRecommendations);
		Collections.sort(sorted, COMPARATOR);
		Collections.reverse(sorted);

		List<QualifiedRecommendation> result = new ArrayList<>(sorted.size());
		long recompressibleBytesRemaining = maxBytesToRecompress;
		for (QualifiedRecommendation originalRecommendation : sorted) {
			if (!originalRecommendation.getRecommendation().uncompressNewEntry) {
				result.add(originalRecommendation);
			} else {
				long bytesToRecompress = originalRecommendation.getNewEntry().getUncompressedSize();
				if (recompressibleBytesRemaining - bytesToRecompress >= 0) {
					result.add(originalRecommendation);
					recompressibleBytesRemaining -= bytesToRecompress;
				} else {
					result.add(new QualifiedRecommendation(originalRecommendation.getOldEntry(),
							originalRecommendation.getNewEntry(), Recommendation.UNCOMPRESS_NEITHER,
							RecommendationReason.RESOURCE_CONSTRAINED));
				}
			}
		}
		return result;
	}

	private final long maxBytesToRecompress;

	private static final Comparator<QualifiedRecommendation> COMPARATOR = new UncompressedNewEntrySizeComparator();

	public TotalRecompressionLimiter(long maxBytesToRecompress) {
		if (maxBytesToRecompress < 0) {
			throw new IllegalArgumentException("maxBytesToRecompress must be non-negative: " + maxBytesToRecompress);
		}
		this.maxBytesToRecompress = maxBytesToRecompress;
	}

	private static class UncompressedNewEntrySizeComparator implements Comparator<QualifiedRecommendation> {
		@Override
		public int compare(QualifiedRecommendation qr1, QualifiedRecommendation qr2) {
			return Long.compare(qr1.getNewEntry().getUncompressedSize(), qr2.getNewEntry().getUncompressedSize());
		}
	}
}
