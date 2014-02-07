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

	public Locals() {
	}

	public Locals(Locals locals2, Type thisType) {
		locals.addAll(locals2.locals);
		Local local0 = locals.get(0);
		if (!local0.nat.name.equals("this"))
			throw new IllegalStateException();
		locals.set(0, new Local(0, new NameAndType("this", thisType)));
		argCnt = locals2.argCnt;
		max = locals2.max;
	}

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

	public Locals add(String name, Type type) {
		add(new NameAndType(name, type));
		return this;
	}

	public void addThis(Type type) {
		Local res = new Local(locals.size(), new NameAndType("this", type));
		locals.add(0, res);
		max = Math.max(max, locals.size());
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