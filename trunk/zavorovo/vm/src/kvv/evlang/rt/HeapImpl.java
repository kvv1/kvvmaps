package kvv.evlang.rt;

import kvv.evlang.rt.RTContext.Type;

public class HeapImpl implements Heap {

	private final Type[] types;

	private static class Entry {
		int typeIdx;
		boolean marked;
		short[] data;
	}

	private final Entry[] entries;

	public HeapImpl(int sz, Type[] types) {
		entries = new Entry[sz];
		this.types = types;
	}

	@Override
	public short alloc(int typeIdx) {
		for (short i = 1; i < entries.length; i++)
			if (entries[i] == null) {
				entries[i] = new Entry();
				entries[i].typeIdx = typeIdx;
				entries[i].data = new short[types[typeIdx].sz];
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
		int mask = types[entry.typeIdx].mask;
		for (int i = 0; i < types[entry.typeIdx].sz; i++) {
			if ((mask & 1) != 0)
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
			else
				entries[i] = null;
		}
	}

}
