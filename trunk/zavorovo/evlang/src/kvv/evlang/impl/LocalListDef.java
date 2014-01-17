package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

public class LocalListDef {

	public static class Local {
		public int n;
		public NameAndType nat;

		public Local(int n, NameAndType nat) {
			this.n = n;
			this.nat = nat;
		}
	}

	private List<Local> locals = new ArrayList<Local>();
	private int argCnt;

	private int max;

	public int getMax() {
		return max;
	}

	public void add(NameAndType nat) {
		System.out.println("LOCAL " + nat.name + " : " + locals.size());
		locals.add(new Local(locals.size(), nat));
		max = Math.max(max, locals.size());
	}

	public void endOfArgs() {
		argCnt = locals.size();
	}

	// public Integer getIdx(String name) {
	// Integer idx = get(name);
	// if(idx == null)
	// return null;
	// return argCnt - idx - 1;
	// }

	public int getArgCnt() {
		return argCnt;
	}

	public Local get(String name) {
		for (int i = 0; i < locals.size(); i++)
			if (locals.get(i).nat.name.equals(name))
				return locals.get(i);
		return null;
	}

	public int getSize() {
		return locals.size();
	}

	public void setSize(int size) {
		locals = new ArrayList<Local>(locals.subList(0, size));
	}

	public Local get(int i) {
		return locals.get(i);
	}
}