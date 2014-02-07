package kvv.evlang.rt.heap;

public class Array {
	public int a;
	private final Heap heap;
	private int sz;
	private final boolean refs;

	public Array(Heap heap, boolean refs) {
		this.refs = refs;
		this.heap = heap;
		a = heap.alloc(8, true, true);
	}

	public int size() {
		return sz;
	}

	public short getAt(int idx) {
		return heap.get(a, idx);
	}

	public void setAt(int idx, int val) {
		heap.set(a, idx, val);
	}

	public int add(int val) {
		if (sz == heap.getArraySize(a)) {
			int aa = heap.alloc(sz + 8, true, refs);
			for (int i = 0; i < sz; i++)
				heap.set(aa, i, heap.get(a, i));
			a = aa;
		}
		heap.set(a, sz++, val);
		return sz - 1;
	}

	public void compact() {
		int i = 0;
		for (int k = 0; k < sz; k++) {
			int val = getAt(k);
			if (val != 0) {
				if (i != k) {
					setAt(i, val);
					setAt(k, 0);
				}
				i++;
			}
		}
		sz = i;
	}

	public void clear(short val) {
		for (int i = 0; i < size(); i++)
			if (getAt(i) == val)
				setAt(i, 0);
	}
}