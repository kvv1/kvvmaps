package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Func {
	public int n;
	public CodeRef code;
	public final int retSize;
	public final String name;
	public final LocalListDef locals;

	public int maxStack = -1;

	public Func(String name, LocalListDef locals, int retSize) {
		this.name = name;
		this.locals = locals;
		this.retSize = retSize;
	}

	int getMaxStack() throws ParseException {
		String msg = "function" + " '" + name + "'";
		if (maxStack < 0) {
			EG.dumpStream.print(msg + " ");
			maxStack = code.check(retSize, msg);
			EG.dumpStream.println("maxStack: " + maxStack);
		}
		return maxStack;
	}
}