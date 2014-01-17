package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Event {
	public enum EventType {
		SET, CHANGE
	}

	private final Context context;
	public CodeRef cond;
	public CodeRef handler;

	public Event.EventType type;

	public Event(Context context, CodeRef cond, CodeRef handler, Event.EventType type) {
		this.cond = cond;
		this.handler = handler;
		this.type = type;
		this.context = context;
	}
/*
	public int getCondMaxStack() throws ParseException {
		String msg = "event cond";
		context.dumpStream.print(msg + " ");
		int maxStack = cond.check(1, msg);
		context.dumpStream.println("maxStack: " + maxStack);
		return maxStack;
	}

	public int getHandlerMaxStack() throws ParseException {
		String msg = "event handler";
		context.dumpStream.print(msg + " ");
		int maxStack = handler.check(0, msg);
		context.dumpStream.println("maxStack: " + maxStack);
		return maxStack;
	}
*/	
}