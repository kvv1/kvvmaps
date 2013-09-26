package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Event {
	public enum EventType {
		SET, CHANGE
	}

	public CodeRef cond;
	public CodeRef handler;

	public Event.EventType type;

	public Event(CodeRef cond, CodeRef handler, Event.EventType type) {
		this.cond = cond;
		this.handler = handler;
		this.type = type;
	}

	public int getCondMaxStack() throws ParseException {
		String msg = "event cond";
		EG.dumpStream.print(msg + " ");
		int maxStack = cond.check(1, msg);
		EG.dumpStream.println("maxStack: " + maxStack);
		return maxStack;
	}

	public int getHandlerMaxStack() throws ParseException {
		String msg = "event handler";
		EG.dumpStream.print(msg + " ");
		int maxStack = handler.check(0, msg);
		EG.dumpStream.println("maxStack: " + maxStack);
		return maxStack;
	}
}