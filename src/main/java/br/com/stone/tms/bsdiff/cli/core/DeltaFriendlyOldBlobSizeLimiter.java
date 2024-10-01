package br.com.stone.tms.bsdiff.cli.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeltaFriendlyOldBlobSizeLimiter implements RecommendationModifier {

    /**
   * Create a new limiter that will restrict the total size of the delta-friendly old blob.
   *
   * @param maxSizeBytes the maximum size of the delta-friendly old blob
   */
  public DeltaFriendlyOldBlobSizeLimiter(long maxSizeBytes) {
    if (maxSizeBytes < 0) {
      throw new IllegalArgumentException("maxSizeBytes must be non-negative: " + maxSizeBytes);
    }
    this.maxSizeBytes = maxSizeBytes;
  }
  
  /** The maximum size of the delta-friendly old blob. */
  private final long maxSizeBytes;

  private static final Comparator<QualifiedRecommendation> COMPARATOR =
      new UncompressedOldEntrySizeComparator();


  @Override
  public List<QualifiedRecommendation> getModifiedRecommendations(
      File oldFile, File newFile, List<QualifiedRecommendation> originalRecommendations) {

    List<QualifiedRecommendation> sorted = sortRecommendations(originalRecommendations);

    List<QualifiedRecommendation> result = new ArrayList<>(sorted.size());
    long bytesRemaining = maxSizeBytes - oldFile.length();
    for (QualifiedRecommendation originalRecommendation : sorted) {
      if (!originalRecommendation.getRecommendation().uncompressOldEntry) {
        // Keep the original recommendation, no need to track size since it won't be uncompressed.
        result.add(originalRecommendation);
      } else {
        long extraBytesConsumed =
            originalRecommendation.getOldEntry().getUncompressedSize()
                - originalRecommendation.getOldEntry().getCompressedSize();
        if (bytesRemaining - extraBytesConsumed >= 0) {
          // Keep the original recommendation, but also subtract from the remaining space.
          result.add(originalRecommendation);
          bytesRemaining -= extraBytesConsumed;
        } else {
          // Update the recommendation to prevent uncompressing this tuple.
          result.add(
              new QualifiedRecommendation(
                  originalRecommendation.getOldEntry(),
                  originalRecommendation.getNewEntry(),
                  Recommendation.UNCOMPRESS_NEITHER,
                  RecommendationReason.RESOURCE_CONSTRAINED));
        }
      }
    }
    return result;
  }

  private static List<QualifiedRecommendation> sortRecommendations(
      List<QualifiedRecommendation> originalRecommendations) {
    List<QualifiedRecommendation> sorted =
        new ArrayList<QualifiedRecommendation>(originalRecommendations);
    Collections.sort(sorted, COMPARATOR);
    Collections.reverse(sorted);
    return sorted;
  }

  /** Helper class implementing the sort order described in the class documentation. */
  private static class UncompressedOldEntrySizeComparator
      implements Comparator<QualifiedRecommendation> {
    @Override
    public int compare(QualifiedRecommendation qr1, QualifiedRecommendation qr2) {
      return Long.compare(
          qr1.getOldEntry().getUncompressedSize(), qr2.getOldEntry().getUncompressedSize());
    }
  }
}
