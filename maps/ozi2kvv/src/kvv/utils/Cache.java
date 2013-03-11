package kvv.utils;

import java.util.LinkedList;

public class Cache<T> {
	private IntHashMap<T> cache = new IntHashMap<T>();
	private LinkedList<Integer> list = new LinkedList<Integer>();

	private final int sz;
	
	public Cache(int sz) {
		this.sz = sz;
	}
	
	public T get(int key) {
		return cache.get(key);
	}

	public void put(int key, T val) {
		if (list.size() > sz) {
			cache.remove(list.removeFirst());
		}
		cache.put(key, val);
		list.addLast(key);
	}
}

