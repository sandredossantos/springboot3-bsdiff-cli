package br.com.stone.tms.bsdiff.cli.core.bsdiff;

import java.io.IOException;

class BsDiff {

	  /**
	   * Search the specified arrays for a contiguous sequence of identical bytes, starting at the
	   * specified "start" offsets and scanning as far ahead as possible till one or the other of the
	   * arrays ends or a non-matching byte is found. Returns the length of the matching sequence of
	   * bytes, which may be zero.
	   *
	   * @param oldData the old data to scan
	   * @param oldStart the position in the old data at which to start the scan
	   * @param newData the new data to scan
	   * @param newStart the position in the new data at which to start the scan
	   * @return the number of matching bytes in the two arrays starting at the specified indices; zero
	   * if the first byte fails to match
	   */
	  // Visible for testing only
	  static int lengthOfMatch(
	      final RandomAccessObject oldData,
	      final int oldStart,
	      final RandomAccessObject newData,
	      final int newStart)
	      throws IOException {
	    final int max = Math.min((int) oldData.length() - oldStart, (int) newData.length() - newStart);
	    if (max > 0) {
	      // If max is 0, it's sometimes possible for this seek to seek to length + 1 and throw an
	      // exception unnecessarily.
	      oldData.seek(oldStart);
	      newData.seek(newStart);
	      for (int offset = 0; offset < max; offset++) {
	        if (oldData.readByte() != newData.readByte()) {
	          return offset;
	        }
	      }
	    }

	    return max;
	  }

	  // Visible for testing only
	  static Match searchForMatchBaseCase(
	      final RandomAccessObject groupArray,
	      final RandomAccessObject oldData,
	      final RandomAccessObject newData,
	      final int newStart,
	      final int oldDataRangeStartA,
	      final int oldDataRangeStartB)
	      throws IOException {
	    // Located the start of a matching range (no further search required) or the size of the range
	    // has shrunk to one byte (no further search possible).
	    groupArray.seekToIntAligned(oldDataRangeStartA);
	    final int groupArrayOldDataRangeStartA = groupArray.readInt();
	    final int lengthOfMatchA =
	        lengthOfMatch(oldData, groupArrayOldDataRangeStartA, newData, newStart);
	    groupArray.seekToIntAligned(oldDataRangeStartB);
	    final int groupArrayOldDataRangeStartB = groupArray.readInt();
	    final int lengthOfMatchB =
	        lengthOfMatch(oldData, groupArrayOldDataRangeStartB, newData, newStart);

	    if (lengthOfMatchA > lengthOfMatchB) {
	      return Match.of(groupArrayOldDataRangeStartA, lengthOfMatchA);
	    }

	    return Match.of(groupArrayOldDataRangeStartB, lengthOfMatchB);
	  }

	  /**
	   * Locates the run of bytes in |oldData| which matches the longest prefix of
	   * newData[newStart ... newData.length - 1].
	   * @param groupArray
	   * @param oldData the old data to scan
	   * @param newData the new data to scan
	   * @param newStart the position of the first byte in newData to consider
	   * @param oldDataRangeStartA
	   * @param oldDataRangeStartB
	   * @return a Match containing the length of the matching range, and the position at which the
	   * matching range begins.
	   */
	  // Visible for testing only
	  static Match searchForMatch(
	      final RandomAccessObject groupArray,
	      final RandomAccessObject oldData,
	      final RandomAccessObject newData,
	      final int newStart,
	      final int oldDataRangeStartA,
	      final int oldDataRangeStartB)
	      throws IOException {
	    if (oldDataRangeStartB - oldDataRangeStartA < 2) {
	      return searchForMatchBaseCase(
	          groupArray, oldData, newData, newStart, oldDataRangeStartA, oldDataRangeStartB);
	    }

	    // Cut range in half and search again
	    final int rangeLength = oldDataRangeStartB - oldDataRangeStartA;
	    final int pivot = oldDataRangeStartA + (rangeLength / 2);
	    groupArray.seekToIntAligned(pivot);
	    final int groupArrayPivot = groupArray.readInt();
	    if (BsUtil.lexicographicalCompare(
	            oldData,
	            groupArrayPivot,
	            (int) oldData.length() - groupArrayPivot,
	            newData,
	            newStart,
	            (int) newData.length() - newStart)
	        < 0) {
	      return searchForMatch(groupArray, oldData, newData, newStart, pivot, oldDataRangeStartB);
	    }
	    return searchForMatch(groupArray, oldData, newData, newStart, oldDataRangeStartA, pivot);
	  }

	  static class Match {
	    final int start;
	    final int length;

	    static Match of(int start, int length) {
	      return new Match(start, length);
	    }

	    private Match(int start, int length) {
	      this.start = start;
	      this.length = length;
	    }
	  }
	}
