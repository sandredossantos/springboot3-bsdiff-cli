package br.com.stone.tms.bsdiff.cli.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class DeflateUncompressor implements Uncompressor {

	private boolean nowrap = true;
	private int inputBufferSize = 32768;
	private int outputBufferSize = 32768;
	private Inflater inflater = null;
	private boolean caching = false;

	public boolean isNowrap() {
		return nowrap;
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

	public void setNowrap(boolean nowrap) {
		if (nowrap != this.nowrap) {
			release();
			this.nowrap = nowrap;
		}
	}

	public boolean isCaching() {
		return caching;
	}

	public void setCaching(boolean caching) {
		this.caching = caching;
	}

	protected Inflater createOrResetInflater() {
		Inflater result = inflater;
		if (result == null) {
			result = new Inflater(nowrap);
			if (caching) {
				inflater = result;
			}
		} else {
			result.reset();
		}
		return result;
	}

	public void release() {
		if (inflater != null) {
			inflater.end();
			inflater = null;
		}
	}

	@Override
	public void uncompress(InputStream compressedIn, OutputStream uncompressedOut) throws IOException {
		InflaterInputStream inflaterIn = new InflaterInputStream(compressedIn, createOrResetInflater(),
				inputBufferSize);
		byte[] buffer = new byte[outputBufferSize];
		int numRead = 0;
		while ((numRead = inflaterIn.read(buffer)) >= 0) {
			uncompressedOut.write(buffer, 0, numRead);
		}
		if (!isCaching()) {
			release();
		}
	}
}
