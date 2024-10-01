package br.com.stone.tms.bsdiff.cli.shared;

public class PatchConstants {

	public static final String IDENTIFIER = "GFbFv1_0";

	public static enum CompatibilityWindowId {

		DEFAULT_DEFLATE((byte) 0);

		public final byte patchValue;

		private CompatibilityWindowId(byte patchValue) {
			this.patchValue = patchValue;
		}

		public static CompatibilityWindowId fromPatchValue(byte patchValue) {
			switch (patchValue) {
			case 0:
				return DEFAULT_DEFLATE;
			default:
				return null;
			}
		}
	}

	public static enum DeltaFormat {

		BSDIFF((byte) 0);

		public final byte patchValue;

		private DeltaFormat(byte patchValue) {
			this.patchValue = patchValue;
		}

		public static DeltaFormat fromPatchValue(byte patchValue) {
			switch (patchValue) {
			case 0:
				return BSDIFF;
			default:
				return null;
			}
		}
	}
}
