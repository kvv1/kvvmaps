package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Timer {
	public final int n;
	public CodeRef handler;
	public final String name;
	private final Context context;

	public Timer(Context context, String name, int n) {
		this.name = name;
		this.n = n;
		this.context = context;
	}

	public int getMaxStack() throws ParseException {
		String msg = "timer '" + name + "'";
		context.dumpStream.print(msg + " ");
		int maxStack = handler.check(0, msg);
		context.dumpStream.println("maxStack: " + maxStack);
		return maxStack;
	}
}