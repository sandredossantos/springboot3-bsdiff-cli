package br.com.stone.tms.bsdiff.cli.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MatchingOutputStream extends OutputStream {

	  /**
	   * The bytes to match against.
	   */
	  private final InputStream expectedBytesStream;

	  /**
	   * The buffer for reading bytes from the input stream for matching.
	   */
	  private final byte[] buffer;

	  /**
	   * Constructs a new stream that will match against the the specified {@link InputStream}.
	   * @param expectedBytesStream stream of bytes to expect to see
	   * @param matchBufferSize the number of bytes to reserve for matching against the specified
	   * {@link InputStream}. This
	   */
	  public MatchingOutputStream(InputStream expectedBytesStream, int matchBufferSize) {
	    if (matchBufferSize < 1) {
	      throw new IllegalArgumentException("buffer size must be >= 1");
	    }
	    this.expectedBytesStream = expectedBytesStream;
	    this.buffer = new byte[matchBufferSize];
	  }

	  @Override
	  public void write(int b) throws IOException {
	    int expected = expectedBytesStream.read();
	    if (expected == -1) {
	      throw new MismatchException("EOF reached in expectedBytesStream");
	    }
	    if (expected != b) {
	      throw new MismatchException("Data does not match");
	    }
	  }

	  @Override
	  public void write(byte[] b) throws IOException {
	    write(b, 0, b.length);
	  }

	  @Override
	  public void write(byte[] dataToWrite, int offset, int length) throws IOException {
	    int numReadSoFar = 0;
	    while (numReadSoFar < length) {
	      int maxToRead = Math.min(buffer.length, length - numReadSoFar);
	      int numReadThisLoop = expectedBytesStream.read(buffer, 0, maxToRead);
	      if (numReadThisLoop == -1) {
	        throw new MismatchException("EOF reached in expectedBytesStream");
	      }
	      for (int matchCount = 0; matchCount < numReadThisLoop; matchCount++) {
	        if (buffer[matchCount] != dataToWrite[offset + numReadSoFar + matchCount]) {
	          throw new MismatchException("Data does not match");
	        }
	      }
	      numReadSoFar += numReadThisLoop;
	    }
	  }

	  @Override
	  public void close() throws IOException {
	    expectedBytesStream.close();
	  }

	  /**
	   * Expects the end-of-file to be reached in the associated {@link InputStream}.
	   * @throws IOException if the end-of-file has not yet been reached in the associated
	   * {@link InputStream}
	   */
	  public void expectEof() throws IOException {
	    if (expectedBytesStream.read() != -1) {
	      throw new MismatchException("EOF not reached in expectedBytesStream");
	    }
	  }
	}
