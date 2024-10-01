package br.com.stone.tms.bsdiff.cli.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class DeflateCompressor implements Compressor {

	private int compressionLevel = Deflater.DEFAULT_COMPRESSION;
	private int strategy = Deflater.DEFAULT_STRATEGY;
	private boolean nowrap = true;
	private int inputBufferSize = 32768;
	private int outputBufferSize = 32768;
	private Deflater deflater = null;
	private boolean caching = false;

	public boolean isNowrap() {
		return nowrap;
	}

	public void setNowrap(boolean nowrap) {
		if (nowrap != this.nowrap) {
			release();
			this.nowrap = nowrap;
		}
	}

	public int getCompressionLevel() {
		return compressionLevel;
	}

	public void setCompressionLevel(int compressionLevel) {
		if (compressionLevel < 0 || compressionLevel > 9) {
			throw new IllegalArgumentException("compressionLevel must be in the range [0,9]: " + compressionLevel);
		}
		if (deflater != null && compressionLevel != this.compressionLevel) {
			deflater.reset();
			deflater.setLevel(compressionLevel);
		}
		this.compressionLevel = compressionLevel;
	}

	public int getStrategy() {
		return strategy;
	}

	public void setStrategy(int strategy) {
		if (deflater != null && strategy != this.strategy) {
			deflater.reset();
			deflater.setStrategy(strategy);
		}
		this.strategy = strategy;
	}

	public int getInputBufferSize() {
		return inputBufferSize;
	}

	public void setInputBufferSize(int inputBufferSize) {
		this.inputBufferSize = inputBufferSize;
	}

	public int getOutputBufferSize() {
		return outputBufferSize;
	}

	public void setOutputBufferSize(int outputBufferSize) {
		this.outputBufferSize = outputBufferSize;
	}

	public boolean isCaching() {
		return caching;
	}

	public void setCaching(boolean caching) {
		this.caching = caching;
	}

	protected Deflater createOrResetDeflater() {
		Deflater result = deflater;
		if (result == null) {
			result = new Deflater(compressionLevel, nowrap);
			result.setStrategy(strategy);
			if (caching) {
				deflater = result;
			}
		} else {
			result.reset();
		}
		return result;
	}

	public void release() {
		if (deflater != null) {
			deflater.end();
			deflater = null;
		}
	}

	@Override
	public void compress(InputStream uncompressedIn, OutputStream compressedOut) throws IOException {
		byte[] buffer = new byte[inputBufferSize];
		DeflaterOutputStream deflaterOut = new DeflaterOutputStream(compressedOut, createOrResetDeflater(),
				outputBufferSize);
		int numRead = 0;
		while ((numRead = uncompressedIn.read(buffer)) >= 0) {
			deflaterOut.write(buffer, 0, numRead);
		}
		deflaterOut.finish();
		deflaterOut.flush();
	}
}
