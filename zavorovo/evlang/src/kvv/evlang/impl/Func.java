package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Func {
	private final Context context;
	public int n;
	public CodeRef code;
	public final int retSize;
	public final String name;
	public final LocalListDef locals;

	public int maxStack = -1;

	public Func(Context context, String name, LocalListDef locals, int retSize) {
		this.name = name;
		this.locals = locals;
		this.retSize = retSize;
		this.context = context;
	}

	int getMaxStack() throws ParseException {
		String msg = "function" + " '" + name + "'";
		if (maxStack < 0) {
			context.dumpStream.print(msg + " ");
			maxStack = code.check(retSize, msg);
			context.dumpStream.println("maxStack: " + maxStack);
		}
		return maxStack;
	}
}