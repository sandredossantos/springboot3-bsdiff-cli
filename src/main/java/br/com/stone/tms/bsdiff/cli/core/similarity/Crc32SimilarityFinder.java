package br.com.stone.tms.bsdiff.cli.core.similarity;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.com.stone.tms.bsdiff.cli.core.MinimalZipEntry;

public class Crc32SimilarityFinder extends SimilarityFinder {

	private final Map<Long, List<MinimalZipEntry>> baseEntriesByCrc32 = new HashMap<>();

	public Crc32SimilarityFinder(File baseArchive, Collection<MinimalZipEntry> baseEntries) {
		super(baseArchive, baseEntries);
		for (MinimalZipEntry oldEntry : baseEntries) {
			long crc32 = oldEntry.getCrc32OfUncompressedData();
			List<MinimalZipEntry> entriesForCrc32 = baseEntriesByCrc32.get(crc32);
			if (entriesForCrc32 == null) {
				entriesForCrc32 = new LinkedList<>();
				baseEntriesByCrc32.put(crc32, entriesForCrc32);
			}
			entriesForCrc32.add(oldEntry);
		}
	}

	@Override
	public List<MinimalZipEntry> findSimilarFiles(File newArchive, MinimalZipEntry newEntry) {
		List<MinimalZipEntry> matchedEntries = baseEntriesByCrc32.get(newEntry.getCrc32OfUncompressedData());
		if (matchedEntries == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(matchedEntries);
	}
}
