package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

public class Locals {

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

	public Local add(NameAndType nat) {
		System.out.println("LOCAL " + nat.name + " : " + locals.size());
		Local res = new Local(locals.size(), nat);
		locals.add(res);
		max = Math.max(max, locals.size());
		return res;
	}

	public void endOfArgs() {
		argCnt = locals.size();
	}

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