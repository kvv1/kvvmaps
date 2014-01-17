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

	public void put(Func func) {
		if (func.retType.equals(Type.VOID) && func.name.equals("main")
				&& func.locals.getArgCnt() == 0)
			setMain(func);
		else
			add(func, func.name, funcs.size());
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

	private void add(Func func, String name, int n) {
		func.n = n;
		funcs1.put(n, func);
		funcs.put(name, func);
	}

	private void setMain(Func func) {
		add(func, "<main>", 1);
	}

	public void setInit(Func func) {
		add(func, "<init>", 0);
	}

	public static void main(String[] args) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("a", null);
		map.put("b", "b");
		map.put("c", "c");
		map.put("d", "d");
		map.put("e", "e");

		map.put("a", "a");

		for (String s : map.values()) {
			System.out.println(s);
		}
	}

}
