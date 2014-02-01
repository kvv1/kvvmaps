package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

public class Pool<T> {
	public List<T> data = new ArrayList<T>();

	private final int max;

	public Pool(int max) {
		this.max = max;
	}

	public Integer add(T s) {
		for (int i = 0; i < data.size(); i++)
			if (data.get(i) == s)
				return i;
		if (data.size() < max) {
			data.add(s);
			return data.size() - 1;
		}
		return null;
	}

	public int size() {
		return data.size();
	}
}
