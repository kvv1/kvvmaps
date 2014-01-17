package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.evlang.ParseException;
import kvv.evlang.impl.Event.EventType;
import kvv.evlang.impl.LocalListDef.Local;
import kvv.evlang.rt.BC;
import kvv.evlang.rt.TryCatchBlock;

public class Code {

	public final List<Byte> code = new ArrayList<Byte>();
	public Set<TryCatchBlock> tryCatchBlocks = new HashSet<TryCatchBlock>();
	public Set<Integer> locals = new HashSet<Integer>();

	static Map<BC, Integer> histo = new HashMap<BC, Integer>();

	static void printHisto() {
		for (BC c : histo.keySet()) {
			System.out.println(c.name() + " " + histo.get(c));
		}
	}

	void add(BC c) {
		code.add((byte) c.ordinal());
		Integer n = histo.get(c);
		histo.put(c, n == null ? 1 : n + 1);
	}

	void add(int c) {
		code.add((byte) c);
	}

	public void addAll(Code c) {
		int sz = size();
		code.addAll(c.code);
		for (TryCatchBlock tcb : c.tryCatchBlocks)
			tryCatchBlocks.add(new TryCatchBlock(tcb.from + sz, tcb.to + sz,
					tcb.handler + sz));
		for (int local : c.locals)
			locals.add(local + sz);
	}

	int size() {
		return code.size();
	}

	public void compileEnter(int n) throws ParseException {
		if (n < 0)
			throw new ParseException();
		if (n != 0) {
			add(BC.ENTER);
			add(n);
		}
	}

	void compileLit(short s) {
		if (s >= -32 && s < 32) {
			add(BC.LITSHORT + (s & 0x3F));
		} else {
			add(BC.LIT);
			add(s >>> 8);
			add(s & 0xFF);
		}
	}

	void compileGetreg(int reg) {
		if (reg < 64) {
			add(BC.GETREGSHORT + reg);
		} else {
			add(BC.GETREG);
			add(reg);
		}
	}

	void compileSetreg(int reg) {
		if (reg < 64) {
			add(BC.SETREGSHORT + reg);
		} else {
			add(BC.SETREG);
			add(reg);
		}
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

	void compileGetLocal(int loc) {
		add(BC.GETLOCAL);
		add(loc);
		locals.add(size());
	}

	private void compileSetLocal(int loc) {
		add(BC.SETLOCAL);
		add(loc);
		locals.add(size());
	}

	public static Code dec(Context context, String name) throws ParseException {
		RegisterDescr descr = context.registers.get(name);
		if (descr == null)
			context.throwExc(name + " - ?");
		context.checkROReg(descr);
		Code res = new Code();
		res.add(BC.DEC);
		res.add(descr.reg);
		return res;
	}

	public static Code inc(Context context, String name) throws ParseException {
		RegisterDescr descr = context.registers.get(name);
		if (descr == null)
			context.throwExc(name + " - ?");
		context.checkROReg(descr);
		Code res = new Code();
		res.add(BC.INC);
		res.add(descr.reg);
		return res;
	}

	public static Code callp(Context context, String name, List<Expr> argList)
			throws ParseException {
		Func func = context.getFunc(name, argList);
		Code res = new Code();
		for (Expr c : argList)
			res.addAll(c.getCode());
		res.add(BC.CALL);
		res.add(func.n);
		if (func.retType.getSize() != 0)
			res.add(BC.DROP);
		return res;
	}

	public static Code stop(Context context, String name) throws ParseException {
		Timer timer = context.getCreateTimer(name);
		Code res = new Code();
		res.add(BC.STOPTIMER);
		res.add(timer.n);
		return res;
	}

	public static Code start_ms(Context context, String name, Expr t)
			throws ParseException {
		Timer timer = context.getCreateTimer(name);
		Code res = t.getCode();
		res.add(BC.SETTIMER_MS);
		res.add(timer.n);
		return res;
	}

	public static Code start_s(Context context, String name, Expr t)
			throws ParseException {
		Timer timer = context.getCreateTimer(name);
		Code res = t.getCode();
		res.add(BC.SETTIMER_S);
		res.add(timer.n);
		return res;
	}

	public static Code print(Expr n) throws ParseException {
		Code res = n.getCode();
		res.add(BC.PRINT);
		return res;
	}

	public static Code ret(Context context, Expr n) throws ParseException {
		if (n != null) {
			if (context.currentFunc.retType.getSize() == 0)
				context.throwExc("'return;' expected");
			n.type.checkAssignableTo(context, context.currentFunc.retType);
			Code bytes = n.getCode();
			int locals = context.currentFunc.locals.getMax();
			if (locals == 0)
				bytes.add(BC.RETI);
			else {
				bytes.add(BC.RETI_N);
				bytes.add(locals);
			}
			return bytes;
		} else {
			if (context.currentFunc.retType.getSize() != 0)
				context.throwExc("'return <expr>;' expected");
			Code bytes = new Code();
			int locals = context.currentFunc.locals.getMax();
			if (locals == 0)
				bytes.add(BC.RET);
			else {
				bytes.add(BC.RET_N);
				bytes.add(locals);
			}
			return bytes;
		}
	}

	public static Code throw_(Context context, Expr n) {
		Code res = n.getCode();
		res.add(BC.THROW);
		return res;
	}

	public static Code assign(Context context, String name, Expr t)
			throws ParseException {
		Code res = t.getCode();
		Local val = context.currentFunc.locals.get(name);
		if (val != null) {
			t.type.checkAssignableTo(context, val.nat.type);
			res.compileSetLocal(val.n);
		} else {
			RegisterDescr descr = context.registers.get(name);
			if (descr != null) {
				context.checkROReg(descr);
				t.type.checkAssignableTo(context, descr.type);
				res.compileSetreg(descr.reg);
			} else {
				ExtRegisterDescr extRegisterDescr = context
						.getExtRegisterDescr(name);
				if (extRegisterDescr != null) {
					t.type.checkAssignableTo(context, Type.INT);
					res.compileSetregExt(extRegisterDescr.addr,
							extRegisterDescr.reg);
				} else {
					context.throwExc(name + " - ?");
				}
			}
		}
		return res;
	}

	public int compileBranch(BC bc, int... off) {
		add(bc);
		for (int n : off)
			add(n);
		return code.size();
	}

	public int compileBranch(int off) {
		return compileBranch(BC.BRANCH, off);
	}

	public int compileQBranch(int off) {
		return compileBranch(BC.QBRANCH, off);
	}

	public static Code ifstmt(Expr cond, Code stmt, Code stmt2) {
		Code res = cond.getCode();
		if (stmt2 != null) {
			int a = res.compileQBranch(0);
			res.addAll(stmt);
			int b = res.compileBranch(0);
			res.resolveBranch(a);
			res.addAll(stmt2);
			res.resolveBranch(b);
		} else {
			int a = res.compileQBranch(0);
			res.addAll(stmt);
			res.resolveBranch(a);
		}
		return res;
	}

	public static Code trycatchstmt(Context context, Code tryStmt,
			Code catchStmt, String name) {
		Code res = new Code();

		int from = res.size();
		res.addAll(tryStmt);
		int to = res.size();
		int b = res.compileBranch(0);
		int handler = res.size();
		Local local = context.currentFunc.locals.get(name);
		res.compileSetLocal(local.n);
		res.addAll(catchStmt);
		res.resolveBranch(b);

		res.tryCatchBlocks.add(new TryCatchBlock(from, to, handler));

		return res;
	}

	public static void procDecl(Context context, Type type, String name,
			LocalListDef locals) throws ParseException {
		Func func = context.getCreateFunc(name, locals, type);
		context.currentFunc = func;
	}

	private void compileEnter(Context context) throws ParseException {
		compileEnter(context.currentFunc.locals.getMax()
				- context.currentFunc.locals.getArgCnt());
	}

	private void adjustLocals(Context context) {
		for (int local : locals) {
			int l = code.get(local - 1);
			l = context.currentFunc.locals.getMax() - 1 - l;
			code.set(local - 1, (byte) l);
		}
		locals.clear();
	}

	public static void procCode(Context context, Code bytes)
			throws ParseException {

		Code res = new Code();
		res.compileEnter(context);
		res.addAll(bytes);
		if (context.currentFunc.retType.getSize() == 0)
			res.addAll(ret(context, null));
		else if (context.currentFunc.retType.isRef())
			res.addAll(ret(context, Expr.nullExpr()));
		else
			res.addAll(ret(context, new Expr((short) 0)));
		res.adjustLocals(context);

		context.currentFunc.code = new CodeRef(context, res);
		System.out.println("proc " + context.currentFunc.name + " "
				+ res.size());
	}

	public static void timer(Context context, String name, Code bytes)
			throws ParseException {
		Code res = new Code();
		res.compileEnter(context);
		res.addAll(bytes);
		res.add(BC.RET);
		res.adjustLocals(context);

		Timer timer = context.getCreateTimer(name);
		timer.handler = new CodeRef(context, res);
		System.out.println("timer " + name + " " + res.size());
	}

	public static void onset(Context context, Code cond, Code bytes)
			throws ParseException {
		Code res = new Code();
		res.compileEnter(context);
		res.addAll(bytes);
		res.add(BC.RET);
		res.adjustLocals(context);

		cond.add(BC.RETI);
		context.events.add(new Event(context, new CodeRef(context, cond),
				new CodeRef(context, res), EventType.SET));
		System.out.println("onset " + cond.size() + " " + res.size());
	}

	public static void onchange(Context context, Code cond, Code bytes)
			throws ParseException {
		Code res = new Code();
		res.compileEnter(context);
		res.addAll(bytes);
		res.add(BC.RET);
		res.adjustLocals(context);

		cond.add(BC.RETI);
		context.events.add(new Event(context, new CodeRef(context, cond),
				new CodeRef(context, res), EventType.CHANGE));
		System.out.println("onchange " + cond.size() + " " + res.size());
	}

	public void resolveBranchs(List<Integer> addrs) {
		for (int a : addrs)
			resolveBranch(a);
	}

	public void resolveBranch(int a) {
		code.set(a - 1, (byte) (code.size() - a));
	}

}
