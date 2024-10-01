package br.com.stone.tms.bsdiff.cli.core.similarity;

import java.io.File;
import java.util.Collection;
import java.util.List;

import br.com.stone.tms.bsdiff.cli.core.MinimalZipEntry;

public abstract class SimilarityFinder {

	  protected final File baseArchive;
	  protected final Collection<MinimalZipEntry> baseEntries;
	  
	  public SimilarityFinder(File baseArchive, Collection<MinimalZipEntry> baseEntries) {
	    this.baseArchive = baseArchive;
	    this.baseEntries = baseEntries;
	  }
	
	  public abstract List<MinimalZipEntry> findSimilarFiles(File newArchive, MinimalZipEntry newEntry);
	}
