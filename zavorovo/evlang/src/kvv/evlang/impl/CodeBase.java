package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kvv.evlang.ParseException;
import kvv.evlang.rt.BC;
import kvv.evlang.rt.BC_SHORT;

public class CodeBase {
	public final Context context;

	public final List<Byte> code = new ArrayList<Byte>();
	public final Set<Integer> locals = new HashSet<Integer>();

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

	short size() {
		return (short) code.size();
	}

	void add(BC c) {
		code.add((byte) c.ordinal());
	}

	void addBC(BC_SHORT bc, int n) {
		code.add((byte) (bc.n | (n & 0x0F)));
	}

	void addN(int c) {
		code.add((byte) c);
	}

	// //////////////////////////////////////////////////////////////////////////////////

	void compileLit(short s) {
		Integer poolIdx = context.constPool.add(s);
		if (poolIdx != null) {
			addBC(BC_SHORT.LIT_SHORT, poolIdx);
		} else {
			add(BC.LIT);
			addN(s >>> 8);
			addN(s & 0xFF);
		}
	}

	void compileGetreg(short reg) {
		Integer poolIdx = context.regPool.add(reg);
		if (poolIdx != null) {
			addBC(BC_SHORT.GETREG_SHORT, poolIdx);
		} else {
			add(BC.GETREG);
			addN(reg);
		}
	}

	void compileSetreg(short reg) {
		Integer poolIdx = context.regPool.add(reg);
		if (poolIdx != null) {
			addBC(BC_SHORT.SETREG_SHORT, poolIdx);
		} else {
			add(BC.SETREG);
			addN(reg);
		}
	}

	void compileGetLocal(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy locals");
		addBC(BC_SHORT.GETLOCAL_SHORT, n);
		locals.add((int) size());
	}

	void compileSetLocal(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy locals");
		addBC(BC_SHORT.SETLOCAL_SHORT, n);
		locals.add((int) size());
	}

	protected void compileSetregExt(int addr, int reg) {
		add(BC.SETEXTREG);
		addN(addr);
		addN(reg);
	}

	protected void compileGetregExt(int addr, int reg) {
		add(BC.GETEXTREG);
		addN(addr);
		addN(reg);
	}

	protected void compileCall(int n) {
		if (n < 16) {
			addBC(BC_SHORT.CALL_SHORT, n);
		} else {
			add(BC.CALL);
			addN(n);
		}
	}

	public void compileVCall(int argCnt, int n) {
		add(BC.VCALL);
		addN((argCnt << 4) + n);
	}

	protected void compileRetI(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy locals");
		addBC(BC_SHORT.RETI_SHORT, n);
	}

	protected void compileRet(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy locals");
		addBC(BC_SHORT.RET_SHORT, n);
	}

	public void compileEnter(int extraLocals) throws ParseException {
		if (extraLocals > 0)
			addBC(BC_SHORT.ENTER_SHORT, extraLocals);
	}

	void compileGetfield(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy fields");
		addBC(BC_SHORT.GETFIELD_SHORT, n);
	}

	void compileSetfield(int n) throws ParseException {
		if (n >= 16)
			context.throwExc("too mamy fields");
		addBC(BC_SHORT.SETFIELD_SHORT, n);
	}

	public void compileNew(int n) {
		if (n >= 16) {
			add(BC.NEW);
			addN(n);
		} else {
			addBC(BC_SHORT.NEW_SHORT, n);
		}
	}

	public void compileNewObjArr() {
		add(BC.NEWOBJARR);
	}

	public void compileNewIntArr() {
		add(BC.NEWINTARR);
	}

	public void compileSetArray() {
		add(BC.SETARRAY);
	}

	public void compileGetArray() {
		add(BC.GETARRAY);
	}

	public void compileArrayLength() {
		add(BC.ARRAYLENGTH);
	}
}
