package kvv.evlang.rt;

import java.util.List;

import kvv.evlang.rt.HeapImpl.Array;

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
	public final Event[] events;
	public final Func[] funcs;
	public final TryCatchBlock[] tryCatchBlocks;
	public final Short[] constPool;
	public final Short[] regPool;
	public final Byte[] refs;
	public final Type[] types;
	public final Array timers;

	public final Heap heap;

	public TryCatchBlock findTryCatchBlock(int ip) {
		for (TryCatchBlock tcb : tryCatchBlocks) {
			if (ip > tcb.from && ip <= tcb.to)
				return tcb;
		}
		return null;
	}

	public RTContext(List<Byte> codeArr, Event[] events, Func[] funcs,
			TryCatchBlock[] tryCatchBlocks, Short[] constPool, Short[] regPool,
			Byte[] refs, Type[] types) {
		this.codeArr = codeArr;
		this.events = events;
		this.funcs = funcs;
		this.tryCatchBlocks = tryCatchBlocks;
		this.constPool = constPool;
		this.regPool = regPool;
		this.refs = refs;
		this.types = types;
		heap = new HeapImpl(64, types);
		timers = new Array(heap);
	}

	public void gc() {
		for (byte b : refs)
			heap.mark(regs[b & 0xFF]);
		heap.mark(timers.a);
		heap.sweep();
	}

	final static int TIMER_FUNC_IDX = 0;
	final static int TIMER_CNT_IDX = 1;

	public void setTimer(short obj, short ms) {
		timers.clear(obj);
		timers.add(obj);
		heap.set(obj, TIMER_CNT_IDX, ms);
	}

	public void stopTimer(short a) {
		timers.clear(a);
		heap.set(a, TIMER_CNT_IDX, 0);
	}
}
