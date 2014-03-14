package kvv.evlang.rt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

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

	public static class UIReg {
		public final int reg;
		public final String text;
		public final int type;

		public UIReg(int reg, String text, int type) {
			this.reg = reg;
			this.text = text;
			this.type = type;
		}
	}

	static class TTEntry {
		boolean flag;
		short val;

		static void arrayClear(TTEntry[] arr, int val) {
			for (int i = 0; i < arr.length; i++) {
				if (arr[i].val == val) {
					arr[i].val = 0;
					break;
				}
			}
		}

		static void arraySet(TTEntry[] arr, int val) {
			arrayClear(arr, val);
			for (int i = 0; i < arr.length; i++) {
				if (arr[i].val == 0) {
					arr[i].val = (short) val;
					arr[i].flag = true;
					break;
				}
			}
		}

	}

	public final UIReg[] uiRegs;
	public final short[] funcs;
	public final TryCatchBlock[] tryCatchBlocks;
	public final Short[] constPool;
	public final Short[] regPool;
	public final Byte[] refs;
	public final Type[] types;
	public final List<Byte> code;

	public final TTEntry[] timers = new TTEntry[32];
	public final TTEntry[] triggers = new TTEntry[32];
	public int currentTT;
	public final short[] regs = new short[256];

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
			Byte[] refs, final Type[] types, UIReg[] uiRegs) {
		this.code = codeArr;
		this.funcs = funcs;
		this.tryCatchBlocks = tryCatchBlocks;
		this.constPool = constPool;
		this.regPool = regPool;
		this.refs = refs;
		this.types = types;
		this.uiRegs = uiRegs;
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

		for (int i = 0; i < timers.length; i++)
			timers[i] = new TTEntry();

		for (int i = 0; i < triggers.length; i++)
			triggers[i] = new TTEntry();
	}

	public static final int TIMER_CNT_IDX = 0;
	public static final int TIMER_RUN_FUNC_IDX = 0;

	public static final int TRIGGER_VAL_IDX = 0;
	public static final int TRIGGER_VAL_FUNC_IDX = 0;
	public static final int TRIGGER_HANDLE_FUNC_IDX = 1;

	public void setTimer(short obj, short ms) {
		TTEntry.arraySet(timers, obj);
		heap.set(obj, TIMER_CNT_IDX, ms);
	}

	public void stopTimer(short obj) {
		TTEntry.arrayClear(timers, obj);
		heap.set(obj, TIMER_CNT_IDX, 0);
	}

	public void setTrigger(short obj, short initVal) {
		TTEntry.arraySet(triggers, obj);
		heap.set(obj, TRIGGER_VAL_IDX, initVal);
	}

	public void stopTrigger(short obj) {
		TTEntry.arrayClear(triggers, obj);
		heap.set(obj, TRIGGER_VAL_IDX, 0);
	}

	public void gc() {
		heap.startMark();
		for (byte b : refs)
			heap.mark(regs[b & 0xFF]);

		for (TTEntry e : timers)
			heap.mark(e.val);

		for (TTEntry e : triggers)
			heap.mark(e.val);

		for (int spOff = 0; spOff < stack.depth(); spOff++)
			heap.mark(stack.pick(spOff));

		heap.mark(currentTT);

		heap.markClosure();
		heap.sweep();
	}

	public byte[] dump() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		dos.writeByte(regs.length);
		for (UIReg reg : uiRegs) {
			dos.writeByte(reg.reg);
			dos.writeByte(reg.type);
			dos.writeByte(reg.text.length());
			dos.writeBytes(reg.text);
		}

		dos.writeByte(funcs.length);
		for (int f : funcs)
			dos.writeShort(f);

		dos.writeByte(tryCatchBlocks.length);
		for (TryCatchBlock tcb : tryCatchBlocks) {
			dos.writeShort(tcb.from);
			dos.writeShort(tcb.to);
			dos.writeShort(tcb.handler);
		}

		dos.writeByte(constPool.length);
		for (short s : constPool)
			dos.writeShort(s);

		dos.writeByte(regPool.length);
		for (short s : regPool)
			dos.writeByte(s);

		dos.writeByte(refs.length);
		for (byte ref : refs)
			dos.writeByte(ref);

		dos.writeByte(types.length);
		for (Type type : types) {
			dos.writeByte(type.sz);
			dos.writeShort(type.mask);
			dos.writeByte(type.vtable.length);
			for (short a : type.vtable)
				dos.writeShort(a);
		}

		System.out.println("codeOffset = " + baos.toByteArray().length);

		for (byte b : code)
			dos.write(b);

		dos.close();
		return baos.toByteArray();
	}

	public short getVMethod(short obj, int funcIdx) {
		return types[heap.getTypeIdx(obj)].vtable[funcIdx];
	}

}
