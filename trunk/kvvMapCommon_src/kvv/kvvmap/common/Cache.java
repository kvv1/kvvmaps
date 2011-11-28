package kvv.kvvmap.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Cache<K, V> {
	private final int sz;
	private LinkedList<K> list = new LinkedList<K>();
	private Map<K, V> map = new HashMap<K, V>();

	public Cache(int sz) {
		this.sz = sz;
	}

	public synchronized final Set<K> keySet() {
		return map.keySet();
	}
	
	public synchronized final void remove(K key) {
		V v = map.get(key);
		list.remove(key);
		map.remove(key);
		if(v != null)
			dispose(v);
	}
	
	public synchronized final void put(K key, V value) {
		if (map.containsKey(key))
			return;
		if (list.size() >= sz) {
			K k = list.getLast();
			V v = map.get(k);
			map.remove(k);
			list.remove(k);
			dispose(v);
		}
		list.addFirst(key);
		map.put(key, value);
	}

	protected void dispose(V v) {
	}

	public synchronized final V get(K key) {
		if (!map.containsKey(key))
			return null;
		list.remove(key);
		list.addFirst(key);
		return map.get(key);
	}

	public synchronized void clear() {
		for(K k : list) {
			V v = map.get(k);
			if(v != null)
				dispose(v);
		}
		map.clear();
		list.clear();
		
		list = new LinkedList<K>();
		new HashMap<K, V>();
	}
}
