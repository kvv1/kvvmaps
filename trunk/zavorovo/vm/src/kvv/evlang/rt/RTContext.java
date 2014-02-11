package kvv.evlang.rt;

import java.util.List;

import kvv.evlang.rt.heap.Array;
import kvv.evlang.rt.heap.Heap;
import kvv.evlang.rt.heap.Heap2;

public class RTContext {
	public static class Type {
		public final int sz;
		public final int mask;
		public final short[] vtable;

		public Type(int sz, int mask, short[] vtable) {
			this.sz = sz;
			this.mask = mask;
			this.vtable = vtable;
		}
	}

	public final List<Byte> codeArr;
	public final short[] regs = new short[256];
	public final short[] funcs;
	public final TryCatchBlock[] tryCatchBlocks;
	public final Short[] constPool;
	public final Short[] regPool;
	public final Byte[] refs;
	public final Type[] types;
	public final Array timers;
	public final Array triggers;

	public final Heap heap;

	public final Stack stack = new StackImpl();

	public TryCatchBlock findTryCatchBlock(int ip) {
		for (TryCatchBlock tcb : tryCatchBlocks) {
			if (ip > tcb.from && ip <= tcb.to)
				return tcb;
		}
		return null;
	}

	public RTContext(List<Byte> codeArr, short[] funcs,
			TryCatchBlock[] tryCatchBlocks, Short[] constPool, Short[] regPool,
			Byte[] refs, final Type[] types) {
		this.codeArr = codeArr;
		this.funcs = funcs;
		this.tryCatchBlocks = tryCatchBlocks;
		this.constPool = constPool;
		this.regPool = regPool;
		this.refs = refs;
		this.types = types;
		// heap = new HeapImpl(64, types);
		heap = new Heap2(128) {
			@Override
			protected int getTypeSize(int typeIdx) {
				return types[typeIdx].sz;
			}

			@Override
			protected int getTypeMask(int typeIdx) {
				return types[typeIdx].mask;
			}

			@Override
			protected void gc() {
				RTContext.this.gc();
			}
		};
		timers = new Array(heap, true);
		triggers = new Array(heap, true);
	}

	public static final int TIMER_CNT_IDX = 0;
	public static final int TIMER_RUN_FUNC_IDX = 0;

	public static final int TRIGGER_VAL_IDX = 0;
	public static final int TRIGGER_VAL_FUNC_IDX = 0;
	public static final int TRIGGER_HANDLE_FUNC_IDX = 1;

	public void setTimer(short obj, short ms) {
		timers.clear(obj);
		timers.add(obj);
		heap.set(obj, TIMER_CNT_IDX, ms);
	}

	public void stopTimer(short a) {
		timers.clear(a);
		heap.set(a, TIMER_CNT_IDX, 0);
	}

	public void setTrigger(short obj, short initVal) {
		triggers.clear(obj);
		triggers.add(obj);
		heap.set(obj, TRIGGER_VAL_IDX, initVal);
	}

	public void stopTrigger(short obj) {
		triggers.clear(obj);
		heap.set(obj, TRIGGER_VAL_IDX, 0);
	}

	public void gc() {
		heap.startMark();
		for (byte b : refs)
			heap.mark(regs[b & 0xFF]);
		heap.mark(timers.a);
		heap.mark(triggers.a);

		for (int spOff = 0; spOff < stack.depth(); spOff++)
			heap.mark(stack.pick(spOff));

		// for (int sp = stack.getSP(); sp < stack.size(); sp++)
		// heap.mark(stack.getAt(sp));

		heap.markClosure();
		heap.sweep();
	}

}
