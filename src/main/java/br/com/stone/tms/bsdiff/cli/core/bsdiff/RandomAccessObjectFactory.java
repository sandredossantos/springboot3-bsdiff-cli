package br.com.stone.tms.bsdiff.cli.core.bsdiff;

import java.io.File;
import java.io.IOException;

import br.com.stone.tms.bsdiff.cli.core.bsdiff.RandomAccessObject.RandomAccessByteArrayObject;
import br.com.stone.tms.bsdiff.cli.core.bsdiff.RandomAccessObject.RandomAccessFileObject;
import br.com.stone.tms.bsdiff.cli.core.bsdiff.RandomAccessObject.RandomAccessMmapObject;

public interface RandomAccessObjectFactory {
	public RandomAccessObject create(int size) throws IOException;

	public static final class RandomAccessFileObjectFactory implements RandomAccessObjectFactory {
		private static final String FILE_NAME_PREFIX = "wavsprafof";
		private final String mMode;

		public RandomAccessFileObjectFactory(String mode) {
			mMode = mode;
		}

		@Override
		public RandomAccessObject create(int size) throws IOException {
			return new RandomAccessFileObject(File.createTempFile(FILE_NAME_PREFIX, "temp"), mMode, true);
		}
	}

	public static final class RandomAccessByteArrayObjectFactory implements RandomAccessObjectFactory {
		@Override
		public RandomAccessObject create(int size) {
			return new RandomAccessByteArrayObject(size);
		}
	}

	public static final class RandomAccessMmapObjectFactory implements RandomAccessObjectFactory {
		private static final String FILE_NAME_PREFIX = "wavsprafof";
		private String mMode;

		public RandomAccessMmapObjectFactory(String mode) {
			mMode = mode;
		}

		@Override
		public RandomAccessObject create(int size) throws IOException {
			return new RandomAccessMmapObject(FILE_NAME_PREFIX, mMode, size);
		}
	}
}
