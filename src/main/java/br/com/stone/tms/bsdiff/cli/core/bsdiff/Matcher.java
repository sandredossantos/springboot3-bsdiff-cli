package br.com.stone.tms.bsdiff.cli.core.bsdiff;

import java.io.IOException;

interface Matcher {
	NextMatch next() throws IOException, InterruptedException;

	static class NextMatch {
		final boolean didFindMatch;
		final int oldPosition;
		final int newPosition;

		static NextMatch of(boolean didFindMatch, int oldPosition, int newPosition) {
			return new NextMatch(didFindMatch, oldPosition, newPosition);
		}

		private NextMatch(boolean didFindMatch, int oldPosition, int newPosition) {
			this.didFindMatch = didFindMatch;
			this.oldPosition = oldPosition;
			this.newPosition = newPosition;
		}
	}
}
