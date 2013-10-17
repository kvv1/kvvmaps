package kvv.evlang.rt;

import java.util.List;

public abstract class RTContext {
	public abstract void setExtReg(int addr, int reg, int pop);

	public abstract int getExtReg(int addr, int reg);

	public static class Event {
		public static final int TYPE_SET = 0;
		public static final int TYPE_CHANGE = 1;

		int cond;
		int handler;
		int state;
		int type;

		public Event(int cond, int handler, int type) {
			this.cond = cond;
			this.handler = handler;
			this.type = type;
		}
	}

	public static class Timer {
		int cnt;
		int handler;

		public Timer(int handler) {
			this.handler = handler;
		}
	}

	public static class Func {
		int code;

		public Func(int code) {
			this.code = code;
		}
	}

	public List<Byte> codeArr;
	public int[] regs = new int[256];
	public Timer[] timers;
	public Event[] events;
	public Func[] funcs;

	public RTContext(List<Byte> codeArr, Timer[] timers, Event[] events,
			Func[] funcs) {
		this.codeArr = codeArr;
		this.timers = timers;
		this.events = events;
		this.funcs = funcs;
	}

}
