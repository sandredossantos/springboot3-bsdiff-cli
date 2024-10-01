package br.com.stone.tms.bsdiff.cli.core.bsdiff;

import java.io.IOException;

public interface SuffixSorter {
	RandomAccessObject suffixSort(RandomAccessObject data) throws IOException, InterruptedException;
}
