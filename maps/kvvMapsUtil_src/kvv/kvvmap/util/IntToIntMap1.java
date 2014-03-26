package kvv.kvvmap.util;

import java.util.Arrays;

public final class IntToIntMap1 {
	private long[] data;
	private int size;
	private final int keyBits;
	private final int valueBits;
	private final long keyMask;
	private final long valueMask;
	private final boolean keySigned;
	private final boolean valueSigned;
	private boolean sorted;
	private int grow = 256;

	public IntToIntMap1(int keyBits, boolean keySigned, boolean valueSigned) {
		this.keySigned = keySigned;
		this.valueSigned = valueSigned;
		this.keyBits = keyBits;
		this.valueBits = 64 - keyBits;
		keyMask = -1L << valueBits;
		valueMask = ~keyMask;
		data = alloc(grow);
	}

	private long[] alloc(int sz) {
		long[] data = new long[sz];
		return data;
	}

	public static long addBits(long acc, long n, int bits) {
		long mask = -1L >>> (64 - bits);
		acc <<= bits;
		acc |= n & mask;
		return acc;
	}

	private static void check(long l, int bits, boolean signed) {
		if (signed) {
			long l1 = l >> (bits - 1);
			if (l1 != 0 && l1 != -1L)
				throw new IllegalArgumentException("signed value " + l
						+ " exceeds " + bits + " bits");
		} else {
			if ((l >> bits) != 0)
				throw new IllegalArgumentException("unsigned value " + l
						+ " exceeds " + bits + " bits");
		}
	}

	public void add(long key, long value) {
		check(key, keyBits, keySigned);
		check(value, valueBits, valueSigned);
		if (data.length == size) {
			long[] newdata = alloc(data.length + grow);
			System.arraycopy(data, 0, newdata, 0, size);
			data = newdata;
		}
		long l = addBits(key, value, valueBits);
		data[size++] = l;
		sorted = false;
	}

	private void sort() {
		Arrays.sort(data, 0, size);
		sorted = true;
	}

	public void compact() {
		if (data.length != size) {
			long[] newdata = new long[size];
			System.arraycopy(data, 0, newdata, 0, size);
			data = newdata;
			sort();
		}
	}

	public int getIdx(long key) {
		if (!sorted)
			sort();

		long key1 = key << valueBits;
		int idx = binarySearch(data, 0, size, key1);
		if (idx < 0)
			idx = ~idx;
		if (idx >= size)
			return -1;
		if ((data[idx] & keyMask) != key1)
			return -1;
		return idx;
	}

	public long getValue(int idx) {
		long v = data[idx] & valueMask;
		if (valueSigned)
			v = (v << keyBits) >> keyBits;
		return v;
	}

	public void clear() {
		size = 0;
		data = alloc(grow);
	}

	public int size() {
		return size;
	}

	public long getKey(int i) {
		if(keySigned)
			return data[i] >> valueBits;
		else
			return data[i] >>> valueBits;
	}

	private final long[] emptyArray = {};

	public long[] getValues(long key) {
		int idx = getIdx(key);
		if (idx == -1)
			return emptyArray;

		long key1 = key << valueBits;
		int len = 0;
		for (int i = idx; i < data.length; i++) {
			if ((data[i] & keyMask) == key1)
				len++;
		}

		long[] res = new long[len];
		for (int i = 0; i < len; i++) {
			res[i] = getValue(i);
		}

		return res;
	}

	private static int binarySearch(long[] a, int fromIndex, int toIndex,
			long key) {
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			long midVal = a[mid];

			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	// public void removeValue(long val) {
	// for(int i = idx; i < data.length; i++) {
	// if ((data[i] & keyMask) == key1)
	// len++;
	// }
	// sorted = false;
	// }
}