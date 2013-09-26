package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Timer {
	public int n;
	public CodeRef handler;
	public String name;

	public Timer(String name, int n) {
		this.name = name;
		this.n = n;
	}

	public int getMaxStack() throws ParseException {
		String msg = "timer '" + name + "'";
		EG.dumpStream.print(msg + " ");
		int maxStack = handler.check(0, msg);
		EG.dumpStream.println("maxStack: " + maxStack);
		return maxStack;
	}
}