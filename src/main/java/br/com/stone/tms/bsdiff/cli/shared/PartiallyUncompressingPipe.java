package br.com.stone.tms.bsdiff.cli.shared;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PartiallyUncompressingPipe implements Closeable {

	private final DeflateUncompressor uncompressor;
	private final CountingOutputStream out;
	private final byte[] copyBuffer;

	public static enum Mode {
		COPY, UNCOMPRESS_WRAPPED, UNCOMPRESS_NOWRAP,
	}

	public PartiallyUncompressingPipe(OutputStream out, int copyBufferSize) {
		this.out = new CountingOutputStream(out);
		uncompressor = new DeflateUncompressor();
		uncompressor.setCaching(true);
		copyBuffer = new byte[copyBufferSize];
	}

	public long pipe(InputStream in, Mode mode) throws IOException {
		long bytesWrittenBefore = out.getNumBytesWritten();
		if (mode == Mode.COPY) {
			int numRead = 0;
			while ((numRead = in.read(copyBuffer)) >= 0) {
				out.write(copyBuffer, 0, numRead);
			}
		} else {
			uncompressor.setNowrap(mode == Mode.UNCOMPRESS_NOWRAP);
			uncompressor.uncompress(in, out);
		}
		out.flush();
		return out.getNumBytesWritten() - bytesWrittenBefore;
	}

	public long getNumBytesWritten() {
		return out.getNumBytesWritten();
	}

	@Override
	public void close() throws IOException {
		uncompressor.release();
		out.close();
	}
}
