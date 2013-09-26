package kvv.evlang.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class FuncDefList {
	private Map<String, Func> funcs = new LinkedHashMap<String, Func>();
	private Map<Integer, Func> funcs1 = new LinkedHashMap<Integer, Func>();

	{
		funcs.put("<init>", null);
		funcs.put("<main>", null);
		funcs1.put(0, null);
		funcs1.put(1, null);
	}
	
	public Func get(String name) {
		return funcs.get(name);
	}

	public void put(String name, Func func) {
		add(func, funcs.size(), name);
	}

	public int size() {
		return funcs.size();
	}

	public Collection<Func> values() {
		return funcs.values();
	}

	public Func get(int idx) {
		return funcs1.get(idx);
	}

	private void add(Func func, int n, String name) {
		func.n = n;
		func.name = name;
		funcs1.put(n, func);
		funcs.put(name, func);
	}
	
	public void setMain(Func func) {
		add(func, 1, "<main>");
	}

	public void setInit(Func func) {
		add(func, 0, "<init>");
	}

	public static void main(String[] args) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("a", null);
		map.put("b", "b");
		map.put("c", "c");
		map.put("d", "d");
		map.put("e", "e");
		
		map.put("a", "a");
		
		for(String s : map.values()) {
			System.out.println(s);
		}
	}
	
}
