package kvv.evlang.rt;

import java.util.List;

public class RTContext {
	public static class Event {
		public static final int TYPE_SET = 0;
		public static final int TYPE_CHANGE = 1;

		short cond;
		short handler;
		int state;
		int type;

		public Event(short cond, short handler, int type) {
			this.cond = cond;
			this.handler = handler;
			this.type = type;
		}
	}

	public static class Timer {
		int cnt;
		short handler;

		public Timer(short handler) {
			this.handler = handler;
		}
	}

	public static class Func {
		short code;

		public Func(short code) {
			this.code = code;
		}
	}

	public static class Type {
		int sz;
		int mask;

		public Type(int sz, int mask) {
			this.sz = sz;
			this.mask = mask;
		}
	}

	public final List<Byte> codeArr;
	public final short[] regs = new short[256];
	public final Timer[] timers;
	public final Event[] events;
	public final Func[] funcs;
	public final TryCatchBlock[] tryCatchBlocks;
	public final Short[] constPool;
	public final Short[] regPool;
	public final Byte[] refs;
	public final Type[] types;

	public final Heap heap;

	public TryCatchBlock findTryCatchBlock(int ip) {
		for (TryCatchBlock tcb : tryCatchBlocks) {
			if (ip > tcb.from && ip <= tcb.to)
				return tcb;
		}
		return null;
	}

	public RTContext(List<Byte> codeArr, Timer[] timers, Event[] events,
			Func[] funcs, TryCatchBlock[] tryCatchBlocks, Short[] constPool,
			Short[] regPool, Byte[] refs, Type[] types) {
		this.codeArr = codeArr;
		this.timers = timers;
		this.events = events;
		this.funcs = funcs;
		this.tryCatchBlocks = tryCatchBlocks;
		this.constPool = constPool;
		this.regPool = regPool;
		this.refs = refs;
		this.types = types;
		heap = new HeapImpl(64, types);
	}

	public void gc() {
		for (byte b : refs)
			heap.mark(regs[b & 0xFF]);
		heap.sweep();
	}
}
