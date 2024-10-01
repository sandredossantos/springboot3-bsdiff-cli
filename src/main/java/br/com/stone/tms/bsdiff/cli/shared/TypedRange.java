package br.com.stone.tms.bsdiff.cli.shared;

public class TypedRange<T> implements Comparable<TypedRange<T>> {

	private final long offset;
	private final long length;
	private final T metadata;

	public TypedRange(long offset, long length, T metadata) {
		this.offset = offset;
		this.length = length;
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "offset " + offset + ", length " + length + ", metadata " + metadata;
	}

	public long getOffset() {
		return offset;
	}

	public long getLength() {
		return length;
	}

	public T getMetadata() {
		return metadata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (length ^ (length >>> 32));
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + (int) (offset ^ (offset >>> 32));
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
		TypedRange<?> other = (TypedRange<?>) obj;
		if (length != other.length)
			return false;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!metadata.equals(other.metadata))
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}

	@Override
	public int compareTo(TypedRange<T> other) {
		if (getOffset() < other.getOffset()) {
			return -1;
		} else if (getOffset() > other.getOffset()) {
			return 1;
		}
		return 0;
	}
}
