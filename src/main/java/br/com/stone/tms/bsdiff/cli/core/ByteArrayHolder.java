package br.com.stone.tms.bsdiff.cli.core;

import java.util.Arrays;

public class ByteArrayHolder {

	private final byte[] data;

	public ByteArrayHolder(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ByteArrayHolder other = (ByteArrayHolder) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}
}
