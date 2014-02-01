package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.evlang.ParseException;
import kvv.evlang.rt.BC;

public class CodeBase {
	public final Context context;

	public final List<Byte> code = new ArrayList<Byte>();
	public final Set<Integer> locals = new HashSet<Integer>();

	static Map<BC, Integer> histo = new HashMap<BC, Integer>();

	public CodeBase(Context context) {
		this.context = context;
	}

	protected void adjustLocals() {
		for (int local : locals) {
			byte bc = code.get(local - 1);
			bc = (byte) ((bc & 0xF0) | (context.currentFunc.locals.getMax() - 1 - (bc & 0x0F)));
			code.set(local - 1, bc);
		}
		locals.clear();
	}

	static void printHisto() {
		for (BC c : histo.keySet()) {
			System.out.println(c.name() + " " + histo.get(c));
		}
	}

	short size() {
		return (short) code.size();
	}

	void add(BC c) {
		code.add((byte) c.ordinal());
		Integer n = histo.get(c);
		histo.put(c, n == null ? 1 : n + 1);
	}

	void add(int c) {
		code.add((byte) c);
	}

	// //////////////////////////////////////////////////////////////////////////////////

	void compileLit(short s) {
		Integer poolIdx = context.constPool.add(s);
		if (poolIdx != null) {
			add(BC.LIT_SHORT | poolIdx);
		} else {
			add(BC.LIT);
			add(s >>> 8);
			add(s & 0xFF);
		}
	}

	void compileGetreg(short reg) {
		Integer poolIdx = context.regPool.add(reg);
		if (poolIdx != null) {
			add(BC.GETREG_SHORT | poolIdx);
		} else {
			add(BC.GETREG);
			add(reg);
		}
	}

	void compileSetreg(short reg) {
		Integer poolIdx = context.regPool.add(reg);
		if (poolIdx != null) {
			add(BC.SETREG_SHORT | poolIdx);
		} else {
			add(BC.SETREG);
			add(reg);
		}
	}

	void compileGetLocal(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy locals");
		add(BC.GETLOCAL_SHORT | n);
		locals.add((int) size());
	}

	void compileSetLocal(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy locals");
		add(BC.SETLOCAL_SHORT | n);
		locals.add((int) size());
	}

	protected void compileSetregExt(int addr, int reg) {
		add(BC.SETEXTREG);
		add(addr);
		add(reg);
	}

	protected void compileGetregExt(int addr, int reg) {
		add(BC.GETEXTREG);
		add(addr);
		add(reg);
	}

	protected void compileCall(int n) {
		if (n < 16) {
			add(BC.CALL_SHORT | n);
		} else {
			add(BC.CALL);
			add(n);
		}
	}

	protected void compileRetI(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy locals");
		add(BC.RETI_SHORT | n);
	}

	protected void compileRet(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy locals");
		add(BC.RET_SHORT | n);
	}

	public void compileEnter(int n) throws ParseException {
		add(BC.ENTER_SHORT | n);
	}

	void compileGetfield(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy fields");
		add(BC.GETFIELD_SHORT | n);
	}

	void compileSetfield(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy fields");
		add(BC.SETFIELD_SHORT | n);
	}

}