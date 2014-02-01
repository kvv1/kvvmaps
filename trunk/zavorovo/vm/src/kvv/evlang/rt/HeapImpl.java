package kvv.evlang.rt;

import kvv.evlang.rt.RTContext.Type;

public class HeapImpl implements Heap {

	private final Type[] types;

	private static class Entry {
		int typeIdx_arrSize;
		boolean marked;
		boolean array;
		boolean objArray;
		short[] data;
	}

	private final Entry[] entries;

	public HeapImpl(int sz, Type[] types) {
		entries = new Entry[sz];
		this.types = types;
	}

	@Override
	public short alloc(int typeIdx_arrSize, boolean array, boolean objArray) {
		for (short i = 1; i < entries.length; i++)
			if (entries[i] == null) {
				entries[i] = new Entry();
				entries[i].typeIdx_arrSize = typeIdx_arrSize;
				entries[i].array = array;
				entries[i].objArray = objArray;

				if (!array) {
					entries[i].data = new short[types[typeIdx_arrSize].sz];
					System.out.println("allocated type=" + typeIdx_arrSize
							+ " size=" + types[typeIdx_arrSize].sz + " entry="
							+ i);
				} else {
					entries[i].data = new short[typeIdx_arrSize];
					System.out.println("allocated type=[] size="
							+ typeIdx_arrSize + " entry=" + i);
				}
				return i;
			}
		return 0;
	}

	@Override
	public short get(int a, int off) {
		return entries[a].data[off];
	}

	@Override
	public void set(int a, int off, int val) {
		entries[a].data[off] = (short) val;
	}

	@Override
	public void mark(int a) {
		if (a == 0)
			return;
		Entry entry = entries[a];
		if (entry.marked)
			return;
		entry.marked = true;

		if (!entry.array) {
			int mask = types[entry.typeIdx_arrSize].mask;
			for (int i = 0; i < types[entry.typeIdx_arrSize].sz; i++) {
				if ((mask & 1) != 0)
					mark(entry.data[i]);
				mask >>= 1;
			}
		} else {
			if (entry.objArray)
				for (int i = 0; i < entry.typeIdx_arrSize; i++)
					mark(entry.data[i]);
		}
	}

	@Override
	public void sweep() {
		for (int i = 0; i < entries.length; i++) {
			if (entries[i] == null)
				continue;
			if (entries[i].marked)
				entries[i].marked = false;
			else {
				entries[i] = null;
				System.out.println("free entry=" + i);
			}
		}
	}

	@Override
	public int getArraySize(int a) {
		return entries[a].typeIdx_arrSize;
	}

	public static class Array {
		int a;
		private final Heap heap;
		private int sz;

		public Array(Heap heap) {
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
				int aa = heap.alloc(sz + 8, true, true);
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

}
