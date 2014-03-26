package kvv.kvvmap.util;

import java.util.Arrays;

public final class IntToIntMap {
	private long[] data = new long[4096];
	private int size;

	public void add(long l) {
		if (data.length == size) {
			long[] newdata = new long[data.length + 4096];
			System.arraycopy(data, 0, newdata, 0, data.length);
			data = newdata;
		}
		data[size++] = l;
	}

	public void sort() {
		long[] newdata = new long[size];
		System.arraycopy(data, 0, newdata, 0, size);
		data = newdata;
		Arrays.sort(data);
	}

	public int getIdx(long l, long mask) {
		int idx = Arrays.binarySearch(data, l);
		if (idx < 0)
			idx = -(idx + 1);
		if (idx >= size)
			return -1;
		if ((data[idx] & mask) != l)
			return -1;
		return idx;
	}

	public long getAt(int idx) {
		return data[idx];
	}
}
