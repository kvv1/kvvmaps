package kvv.kvvmap.common;

public final class LongSet {
	private int grow = 16;
	private long[] data = new long[grow];
	private int size;

	public void add(long l) {
		if(contains(l))
			return;
		
		if(data.length == size) {
			long[] newdata = new long[data.length + grow];
			System.arraycopy(data, 0, newdata, 0, data.length);
			data = newdata;
		}
		
		data[size++] = l;
	}
	
	public boolean contains(long l) {
		for(int i = 0; i < size; i++)
			if(data[i] == l)
				return true;
		return false;
	}
	
	public boolean removeValue(long n) {
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

	public int size() {
		return size;
	}

	public interface Comparator {
		int compare(long l1, long l2);
	}
	
	public long findMin(Comparator comp) {
		if(size == 0)
			return 0;
		if(size == 1)
			return data[0];
		long l = data[0];
		for(int i = 1; i < size; i++) {
			long l1 = data[i];
			if(comp.compare(l, l1) > 0) {
				l = l1;
			}
		}
		return l;
	}

	public void clear() {
		size = 0;
	}
}
