package kvv.kvvmap.util;

public final class IntArray {

	private int[] data = new int[0];
	private int size;

	public void add(int n) {
		if (size == data.length) {
			int[] newdata = new int[data.length + 1];
			System.arraycopy(data, 0, newdata, 0, data.length);
			data = newdata;
		}
		data[size++] = n;
	}

	public boolean removeValue(int n) {
		boolean res = false;
		for (int i = size - 1; i >= 0; i--) {
			if (data[i] == n) {
				System.arraycopy(data, i + 1, data, i, size - i - 1);
				size--;
				res = true;
			}
		}
		return res;
	}

	public int[] values() {
		int[] res = new int[size];
		System.arraycopy(data, 0, res, 0, size);
		return res;
	}

	public int get(int idx) {
		if(idx < 0 || idx >= size)
			throw new ArrayIndexOutOfBoundsException(idx);
		return data[idx];
	}
	
	public int size() {
		return size;
	}

}
