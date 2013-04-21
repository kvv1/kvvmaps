package kvv.controllers.client.page;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class MultiMap<K, V> {
	private HashMap<K, HashSet<V>> map = new HashMap<K, HashSet<V>>();

	public void put(K key, V value) {
		HashSet<V> set = map.get(key);
		if (set == null) {
			set = new HashSet<V>();
			map.put(key, set);
		}
		set.add(value);
	}

	public Set<V> get(K key) {
		return map.get(key);
	}

	public Set<K> keySet() {
		return map.keySet();
	}
}