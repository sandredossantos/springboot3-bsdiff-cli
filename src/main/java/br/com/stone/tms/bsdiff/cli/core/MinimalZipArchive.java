package br.com.stone.tms.bsdiff.cli.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipException;

import br.com.stone.tms.bsdiff.cli.shared.RandomAccessFileInputStream;

public class MinimalZipArchive {

	  /**
	   * Sorts {@link MinimalZipEntry} objects by {@link MinimalZipEntry#getFileOffsetOfLocalEntry()} in
	   * ascending order.
	   */
	  private static final Comparator<MinimalZipEntry> LOCAL_ENTRY_OFFSET_COMAPRATOR =
	      new Comparator<MinimalZipEntry>() {
	        @Override
	        public int compare(MinimalZipEntry o1, MinimalZipEntry o2) {
	          return Long.compare(o1.getFileOffsetOfLocalEntry(), o2.getFileOffsetOfLocalEntry());
	        }
	      };

	  /**
	   * Generate a listing of all of the files in a zip archive in file order and return it. Each entry
	   * is a {@link MinimalZipEntry}, which has just enough information to generate a patch.
	   * @param file the zip file to read
	   * @return such a listing
	   * @throws IOException if anything goes wrong while reading
	   */
	  public static List<MinimalZipEntry> listEntries(File file) throws IOException {
	    try (RandomAccessFileInputStream in = new RandomAccessFileInputStream(file)) {
	      return listEntriesInternal(in);
	    }
	  }

	  /**
	   * Internal implementation of {@link #listEntries(File)}.
	   * @param in the input stream to read from
	   * @return see {@link #listEntries(File)}
	   * @throws IOException if anything goes wrong while reading
	   */
	  private static List<MinimalZipEntry> listEntriesInternal(RandomAccessFileInputStream in)
	      throws IOException {
	    // Step 1: Locate the end-of-central-directory record header.
	    long offsetOfEocd = MinimalZipParser.locateStartOfEocd(in, 32768);
	    if (offsetOfEocd == -1) {
	      // Archive is weird, abort.
	      throw new ZipException("EOCD record not found in last 32k of archive, giving up");
	    }

	    // Step 2: Parse the end-of-central-directory data to locate the central directory itself
	    in.setRange(offsetOfEocd, in.length() - offsetOfEocd);
	    MinimalCentralDirectoryMetadata centralDirectoryMetadata = MinimalZipParser.parseEocd(in);

	    // Step 3: Extract a list of all central directory entries (contiguous data stream)
	    in.setRange(
	        centralDirectoryMetadata.getOffsetOfCentralDirectory(),
	        centralDirectoryMetadata.getLengthOfCentralDirectory());
	    List<MinimalZipEntry> minimalZipEntries =
	        new ArrayList<MinimalZipEntry>(centralDirectoryMetadata.getNumEntriesInCentralDirectory());
	    for (int x = 0; x < centralDirectoryMetadata.getNumEntriesInCentralDirectory(); x++) {
	      minimalZipEntries.add(MinimalZipParser.parseCentralDirectoryEntry(in));
	    }

	    // Step 4: Sort the entries in file order, not central directory order.
	    Collections.sort(minimalZipEntries, LOCAL_ENTRY_OFFSET_COMAPRATOR);

	    // Step 5: Seek out each local entry and calculate the offset of the compressed data within
	    for (int x = 0; x < minimalZipEntries.size(); x++) {
	      MinimalZipEntry entry = minimalZipEntries.get(x);
	      long offsetOfNextEntry;
	      if (x < minimalZipEntries.size() - 1) {
	        // Don't allow reading past the start of the next entry, for sanity.
	        offsetOfNextEntry = minimalZipEntries.get(x + 1).getFileOffsetOfLocalEntry();
	      } else {
	        // Last entry. Don't allow reading into the central directory, for sanity.
	        offsetOfNextEntry = centralDirectoryMetadata.getOffsetOfCentralDirectory();
	      }
	      long rangeLength = offsetOfNextEntry - entry.getFileOffsetOfLocalEntry();
	      in.setRange(entry.getFileOffsetOfLocalEntry(), rangeLength);
	      long relativeDataOffset = MinimalZipParser.parseLocalEntryAndGetCompressedDataOffset(in);
	      entry.setFileOffsetOfCompressedData(entry.getFileOffsetOfLocalEntry() + relativeDataOffset);
	    }

	    // Done!
	    return minimalZipEntries;
	  }
	}
