package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Func {
	public int n;
	public CodeRef code;
	public boolean retSize;
	public String name;
	public int args;

	public int maxStack = -1;

	public Func(int args, boolean f) {
		this.args = args;
		this.retSize = f;
	}

	int getMaxStack() throws ParseException {
		String msg = (retSize ? "function" : "procedure") + " '" + name
				+ "'";
		if (maxStack < 0) {
			EG.dumpStream.print(msg + " ");
			maxStack = code.check(retSize ? 1 : 0, msg);
			EG.dumpStream.println("maxStack: " + maxStack);
		}
		return maxStack;
	}
}