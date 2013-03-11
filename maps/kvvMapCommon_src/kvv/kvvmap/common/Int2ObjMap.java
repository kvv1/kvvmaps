package kvv.kvvmap.common;

import java.util.ArrayList;

public class Int2ObjMap<T> {
	private final IntToIntMap1 map;
	private ArrayList<T> objects = new ArrayList<T>();

	public Int2ObjMap(int keyBits, boolean keySigned) {
		map = new IntToIntMap1(keyBits, keySigned, false);
	}

	public T get(long key) {
		int idx = map.getIdx(key);
		if (idx == -1)
			return null;
		return objects.get((int) map.getValue(idx));
	}

	public void put(long key, T obj) {
		int idx = map.getIdx(key);
		if (idx != -1) {
			objects.set((int) map.getValue(idx), obj);
			return;
		}

		//System.out.println("Int2ObjMap size = " + objects.size());
		map.add(key, map.size());
		objects.add(obj);
	}

	public void clear() {
		objects.clear();
		map.clear();
	}

	public int size() {
		return map.size();
	}

	public T getValueAt(int i) {
		return objects.get((int) map.getValue(i));
	}

	public long getKeyAt(int i) {
		return map.getKey(i);
	}
}
