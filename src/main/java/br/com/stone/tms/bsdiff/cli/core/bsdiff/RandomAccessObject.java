package br.com.stone.tms.bsdiff.cli.core.bsdiff;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public interface RandomAccessObject extends DataInput, DataOutput, Closeable {

	public long length() throws IOException;

	public void seek(long pos) throws IOException;

	public void seekToIntAligned(long pos) throws IOException;

	public static final class RandomAccessFileObject extends RandomAccessFile implements RandomAccessObject {
		private final boolean mShouldDeleteFileOnClose;
		private final File mFile;

		public RandomAccessFileObject(final File tempFile, final String mode) throws IOException {
			this(tempFile, mode, false);
		}

		public RandomAccessFileObject(final File tempFile, final String mode, boolean deleteFileOnClose)
				throws IOException {
			super(tempFile, mode);
			mShouldDeleteFileOnClose = deleteFileOnClose;
			mFile = tempFile;
			if (mShouldDeleteFileOnClose) {
				mFile.deleteOnExit();
			}
		}

		@Override
		public void seekToIntAligned(long pos) throws IOException {
			seek(pos * 4);
		}

		@Override
		public void close() throws IOException {
			super.close();
			if (mShouldDeleteFileOnClose) {
				mFile.delete();
			}
		}
	}

	public static class RandomAccessByteArrayObject implements RandomAccessObject {
		protected ByteBuffer mByteBuffer;

		public RandomAccessByteArrayObject(final byte[] byteArray) {
			mByteBuffer = ByteBuffer.wrap(byteArray);
		}

		public RandomAccessByteArrayObject(final int length) {
			mByteBuffer = ByteBuffer.allocate(length);
		}

		protected RandomAccessByteArrayObject() {
		}

		@Override
		public long length() {
			return mByteBuffer.capacity();
		}

		@Override
		public byte readByte() {
			return mByteBuffer.get();
		}

		@Override
		public int readInt() {
			return mByteBuffer.getInt();
		}

		public void writeByte(byte b) {
			mByteBuffer.put(b);
		}

		@Override
		public void writeInt(int i) {
			mByteBuffer.putInt(i);
		}

		@Override
		public void seek(long pos) {
			if (pos > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"RandomAccessByteArrayObject can only handle seek() " + "addresses up to Integer.MAX_VALUE.");
			}

			mByteBuffer.position((int) pos);
		}

		@Override
		public void seekToIntAligned(long pos) {
			seek(pos * 4);
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public boolean readBoolean() {
			return readByte() != 0;
		}

		@Override
		public char readChar() {
			return mByteBuffer.getChar();
		}

		@Override
		public double readDouble() {
			return mByteBuffer.getDouble();
		}

		@Override
		public float readFloat() {
			return mByteBuffer.getFloat();
		}

		@Override
		public void readFully(byte[] b) {
			mByteBuffer.get(b);
		}

		@Override
		public void readFully(byte[] b, int off, int len) {
			mByteBuffer.get(b, off, len);
		}

		@Override
		public String readLine() {
			throw new UnsupportedOperationException();
		}

		@Override
		public long readLong() {
			return mByteBuffer.getLong();
		}

		@Override
		public short readShort() {
			return mByteBuffer.getShort();
		}

		@Override
		public int readUnsignedByte() {
			return mByteBuffer.get() & 0xff;
		}

		@Override
		public int readUnsignedShort() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String readUTF() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int skipBytes(int n) {
			mByteBuffer.position(mByteBuffer.position() + n);
			return n;
		}

		@Override
		public void write(byte[] b) {
			mByteBuffer.put(b);
		}

		@Override
		public void write(byte[] b, int off, int len) {
			mByteBuffer.put(b, off, len);
		}

		@Override
		public void write(int b) {
			writeByte((byte) b);
		}

		@Override
		public void writeBoolean(boolean v) {
			writeByte(v ? (byte) 1 : (byte) 0);
		}

		@Override
		public void writeByte(int v) {
			writeByte((byte) v);
		}

		@Override
		public void writeBytes(String s) {
			for (int x = 0; x < s.length(); x++) {
				writeByte((byte) s.charAt(x));
			}
		}

		@Override
		public void writeChar(int v) {
			mByteBuffer.putChar((char) v);
		}

		@Override
		public void writeChars(String s) {
			for (int x = 0; x < s.length(); x++) {
				writeChar(s.charAt(x));
			}
		}

		@Override
		public void writeDouble(double v) {
			mByteBuffer.putDouble(v);
		}

		@Override
		public void writeFloat(float v) {
			mByteBuffer.putFloat(v);
		}

		@Override
		public void writeLong(long v) {
			mByteBuffer.putLong(v);
		}

		@Override
		public void writeShort(int v) {
			mByteBuffer.putShort((short) v);
		}

		@Override
		public void writeUTF(String s) {
			throw new UnsupportedOperationException();
		}
	}

	public static final class RandomAccessMmapObject extends RandomAccessByteArrayObject {
		private final boolean mShouldDeleteFileOnRelease;
		private final File mFile;
		private final FileChannel mFileChannel;

		public RandomAccessMmapObject(final RandomAccessFile randomAccessFile, String mode)
				throws IOException, IllegalArgumentException {
			if (randomAccessFile.length() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Only files up to 2GiB in size are supported.");
			}

			FileChannel.MapMode mapMode;
			if (mode.equals("r")) {
				mapMode = FileChannel.MapMode.READ_ONLY;
			} else {
				mapMode = FileChannel.MapMode.READ_WRITE;
			}

			mFileChannel = randomAccessFile.getChannel();
			mByteBuffer = mFileChannel.map(mapMode, 0, randomAccessFile.length());
			mByteBuffer.position(0);
			mShouldDeleteFileOnRelease = false;
			mFile = null;
		}

		@SuppressWarnings("resource")
		public RandomAccessMmapObject(final String tempFileName, final String mode, long length)
				throws IOException, IllegalArgumentException {
			if (length > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"RandomAccessMmapObject only supports file sizes up to " + "Integer.MAX_VALUE.");
			}

			mFile = File.createTempFile(tempFileName, "temp");
			mFile.deleteOnExit();
			mShouldDeleteFileOnRelease = true;

			FileChannel.MapMode mapMode;
			if (mode.equals("r")) {
				mapMode = FileChannel.MapMode.READ_ONLY;
			} else {
				mapMode = FileChannel.MapMode.READ_WRITE;
			}

			RandomAccessFile file = null;
			try {
				file = new RandomAccessFile(mFile, mode);
				mFileChannel = file.getChannel();
				mByteBuffer = mFileChannel.map(mapMode, 0, (int) length);
				mByteBuffer.position(0);
			} catch (IOException e) {
				if (file != null) {
					try {
						file.close();
					} catch (Exception ignored) {
						// Nothing more can be done
					}
				}
				close();
				throw new IOException("Unable to open file", e);
			}
		}

		@SuppressWarnings("resource")
		public RandomAccessMmapObject(final File tempFile, final String mode)
				throws IOException, IllegalArgumentException {
			if (tempFile.length() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Only files up to 2GiB in size are supported.");
			}

			mFile = tempFile;
			mFile.deleteOnExit();
			mShouldDeleteFileOnRelease = true;

			FileChannel.MapMode mapMode;
			if (mode.equals("r")) {
				mapMode = FileChannel.MapMode.READ_ONLY;
			} else {
				mapMode = FileChannel.MapMode.READ_WRITE;
			}

			RandomAccessFile file = null;
			try {
				file = new RandomAccessFile(mFile, mode);
				mFileChannel = file.getChannel();
				mByteBuffer = mFileChannel.map(mapMode, 0, tempFile.length());
				mByteBuffer.position(0);
			} catch (IOException e) {
				if (file != null) {
					try {
						file.close();
					} catch (Exception ignored) {
					}
				}
				close();
				throw new IOException("Unable to open file", e);
			}
		}

		@Override
		public void close() throws IOException {
			if (mFileChannel != null) {
				mFileChannel.close();
			}

			mByteBuffer = null;
			System.gc();

			if (mShouldDeleteFileOnRelease && mFile != null) {
				mFile.delete();
			}

		}
	}
}
