package kvv.evlang.rt;

import java.util.List;

import kvv.evlang.impl.Context.CodeRef;
import kvv.evlang.impl.Context.EventType;


public class RTContext {
	public static class Event {
		CodeRef cond;
		CodeRef handler;
		int state;
		EventType type;
		public Event(CodeRef cond, CodeRef handler, EventType type) {
			this.cond = cond;
			this.handler = handler;
			this.type = type;
		}
	}

	public static class Timer {
		int cnt;
		CodeRef handler;
		public Timer(CodeRef handler) {
			this.handler = handler;
		}
	}
	
	public static class Func {
		CodeRef code;
		public Func(CodeRef code) {
			this.code = code;
		}
	}
	
	public List<Byte> codeArr;
	public int[] regs = new int[256];
	public Timer[] timers;
	public Event[] events;
	public Func[] funcs;

	public RTContext(List<Byte> codeArr, Timer[] timers, Event[] events, Func[] funcs) {
		this.codeArr = codeArr;
		this.timers = timers;
		this.events = events;
		this.funcs = funcs;
	}
}
