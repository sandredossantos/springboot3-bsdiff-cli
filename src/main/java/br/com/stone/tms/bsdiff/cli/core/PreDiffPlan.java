package br.com.stone.tms.bsdiff.cli.core;

import java.util.Iterator;
import java.util.List;

import br.com.stone.tms.bsdiff.cli.shared.JreDeflateParameters;
import br.com.stone.tms.bsdiff.cli.shared.TypedRange;

public class PreDiffPlan {
	/**
	 * The plan for uncompressing the old file, in file order.
	 */
	private final List<TypedRange<Void>> oldFileUncompressionPlan;

	/**
	 * The plan for uncompressing the new file, in file order.
	 */
	private final List<TypedRange<JreDeflateParameters>> newFileUncompressionPlan;

	/**
	 * The plan for recompressing the delta-friendly new file, in file order.
	 */
	private final List<TypedRange<JreDeflateParameters>> deltaFriendlyNewFileRecompressionPlan;

	/**
	 * The recommendations upon which the plans are based.
	 */
	private final List<QualifiedRecommendation> qualifiedRecommendations;

	/**
	 * Constructs a new plan.
	 *
	 * @param qualifiedRecommendations the recommendations upon which the plans are
	 *                                 based
	 * @param oldFileUncompressionPlan the plan for uncompressing the old file, in
	 *                                 file order
	 * @param newFileUncompressionPlan the plan for uncompressing the new file, in
	 *                                 file order
	 */
	public PreDiffPlan(List<QualifiedRecommendation> qualifiedRecommendations,
			List<TypedRange<Void>> oldFileUncompressionPlan,
			List<TypedRange<JreDeflateParameters>> newFileUncompressionPlan) {
		this(qualifiedRecommendations, oldFileUncompressionPlan, newFileUncompressionPlan, null);
	}

	/**
	 * Constructs a new plan.
	 *
	 * @param qualifiedRecommendations              the recommendations upon which
	 *                                              the plans are based
	 * @param oldFileUncompressionPlan              the plan for uncompressing the
	 *                                              old file, in file order
	 * @param newFileUncompressionPlan              the plan for uncompressing the
	 *                                              new file, in file order
	 * @param deltaFriendlyNewFileRecompressionPlan the plan for recompression the
	 *                                              delta-friendly new file, in file
	 *                                              order
	 */
	public PreDiffPlan(List<QualifiedRecommendation> qualifiedRecommendations,
			List<TypedRange<Void>> oldFileUncompressionPlan,
			List<TypedRange<JreDeflateParameters>> newFileUncompressionPlan,
			List<TypedRange<JreDeflateParameters>> deltaFriendlyNewFileRecompressionPlan) {
		ensureOrdered(oldFileUncompressionPlan);
		ensureOrdered(newFileUncompressionPlan);
		ensureOrdered(deltaFriendlyNewFileRecompressionPlan);
		this.qualifiedRecommendations = qualifiedRecommendations;
		this.oldFileUncompressionPlan = oldFileUncompressionPlan;
		this.newFileUncompressionPlan = newFileUncompressionPlan;
		this.deltaFriendlyNewFileRecompressionPlan = deltaFriendlyNewFileRecompressionPlan;
	}

	/**
	 * Ensures that the lists passed into the constructors are ordered and throws an
	 * exception if they are not. Null lists and lists whose size is less than 2 are
	 * ignored.
	 *
	 * @param list the list to check
	 */
	private <T> void ensureOrdered(List<TypedRange<T>> list) {
		if (list != null && list.size() >= 2) {
			Iterator<TypedRange<T>> iterator = list.iterator();
			TypedRange<T> lastEntry = iterator.next();
			while (iterator.hasNext()) {
				TypedRange<T> nextEntry = iterator.next();
				if (lastEntry.compareTo(nextEntry) > 0) {
					throw new IllegalArgumentException("List must be ordered");
				}
			}
		}
	}

	/**
	 * Returns the plan for uncompressing the old file to create the delta-friendly
	 * old file.
	 *
	 * @return the plan
	 */
	public final List<TypedRange<Void>> getOldFileUncompressionPlan() {
		return oldFileUncompressionPlan;
	}

	/**
	 * Returns the plan for uncompressing the new file to create the delta-friendly
	 * new file.
	 *
	 * @return the plan
	 */
	public final List<TypedRange<JreDeflateParameters>> getNewFileUncompressionPlan() {
		return newFileUncompressionPlan;
	}

	/**
	 * Returns the plan for recompressing the delta-friendly new file to regenerate
	 * the original new file.
	 *
	 * @return the plan
	 */
	public final List<TypedRange<JreDeflateParameters>> getDeltaFriendlyNewFileRecompressionPlan() {
		return deltaFriendlyNewFileRecompressionPlan;
	}

	/**
	 * Returns the recommendations upon which the plans are based.
	 *
	 * @return the recommendations
	 */
	public final List<QualifiedRecommendation> getQualifiedRecommendations() {
		return qualifiedRecommendations;
	}
}
