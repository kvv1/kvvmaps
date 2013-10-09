package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kvv.controllers.register.RegisterDescr;
import kvv.evlang.ParseException;
import kvv.evlang.impl.Event.EventType;

public class Code {
	public List<Byte> code = new ArrayList<Byte>();

	static Map<BC, Integer> histo = new HashMap<BC, Integer>();

	static void printHisto() {
		for (BC c : histo.keySet()) {
			System.out.println(c.name() + " " + histo.get(c));
		}
	}

	public void insertEnter(int n) throws ParseException {
		if (n < 0)
			throw new ParseException("ERROR");
		if (n != 0) {
			List<Byte> code1 = new ArrayList<Byte>();
			code1.add((byte) BC.ENTER.ordinal());
			code1.add((byte) n);
			code1.addAll(code);
			code = code1;
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
		code.addAll(c.code);
	}

	private int size() {
		return code.size();
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

	void compileGetLocal(int loc) {
		add(BC.GETLOCAL);
		add(loc);
	}

	private void compileSetLocal(int loc) {
		add(BC.SETLOCAL);
		add(loc);
	}

	public static Code dec(Context context, String name) throws ParseException {
		RegisterDescr descr = context.registers.get(name);
		if (descr == null)
			throw new ParseException(name + " - ?");
		context.checkROReg(descr);
		Code res = new Code();
		res.add(BC.DEC);
		res.add(descr.reg);
		return res;
	}

	public static Code inc(Context context, String name) throws ParseException {
		RegisterDescr descr = context.registers.get(name);
		if (descr == null)
			throw new ParseException(name + " - ?");
		context.checkROReg(descr);
		Code res = new Code();
		res.add(BC.INC);
		res.add(descr.reg);
		return res;
	}

	public static Code callp(Context context, String name, List<Expr> argList)
			throws ParseException {
		Func func = context.getFunc(name, argList.size());
		Code res = new Code();
		for (Expr c : argList)
			res.addAll(c.getCode());
		res.add(BC.CALL);
		res.add(func.n);
		if (func.retSize != 0)
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
			if (context.currentFunc.retSize == 0)
				throw new ParseException("'return;' expected");
			Code bytes = n.getCode();
			int argCnt = context.currentFunc.locals.getArgCnt();
			if (argCnt == 0)
				bytes.add(BC.RETI);
			else {
				bytes.add(BC.RETI_N);
				bytes.add(argCnt);
			}
			return bytes;
		} else {
			if (context.currentFunc.retSize != 0)
				throw new ParseException("'return <expr>;' expected");
			Code bytes = new Code();
			int argCnt = context.currentFunc.locals.getArgCnt();
			if (argCnt == 0)
				bytes.add(BC.RET);
			else {
				bytes.add(BC.RET_N);
				bytes.add(argCnt);
			}
			return bytes;
		}
	}

	public static Code assign(Context context, String name, Expr t)
			throws ParseException {
		Code res = t.getCode();
		Integer val = context.currentFunc.locals.get(name);
		if (val != null) {
			res.compileSetLocal(context.currentFunc.locals.getArgCnt() - val
					- 1);
		} else {
			RegisterDescr descr = context.registers.get(name);
			if (descr == null)
				throw new ParseException(name + " - ?");
			context.checkROReg(descr);
			res.compileSetreg(descr.reg);
		}
		return res;
	}

	public static Code ifstmt(Expr cond, Code stmt, Code stmt2) {
		Code res = cond.getCode();
		if (stmt2 != null) {
			res.add(BC.QBRANCH);
			res.add(stmt.size() + 2);
			res.addAll(stmt);
			res.add(BC.BRANCH);
			res.add(stmt2.size());
			res.addAll(stmt2);
		} else {
			res.add(BC.QBRANCH);
			res.add(stmt.size());
			res.addAll(stmt);
		}
		return res;
	}

	public static void procDecl(Context context, int retSize, String name,
			LocalListDef locals) throws ParseException {
		Func func = context.getCreateFunc(name, locals, retSize);
		context.currentFunc = func;
	}

	private void insertEnter(Context context) throws ParseException {
		insertEnter(context.currentFunc.locals.getMax()
				- context.currentFunc.locals.getArgCnt());
	}

	public static void procCode(Context context, Code bytes)
			throws ParseException {

		bytes.insertEnter(context);

		if (context.currentFunc.retSize == 0)
			bytes.addAll(ret(context, null));
		else
			bytes.addAll(ret(context, new Expr((short) 0)));

		context.currentFunc.code = new CodeRef(context, bytes);
		System.out.println("proc " + context.currentFunc.name + " "
				+ bytes.size());
	}

	public static void timer(Context context, String name, Code bytes)
			throws ParseException {
		bytes.insertEnter(context);

		bytes.add(BC.RET);
		Timer timer = context.getCreateTimer(name);
		timer.handler = new CodeRef(context, bytes);
		System.out.println("timer " + name + " " + bytes.size());
	}

	public static void onset(Context context, Code cond, Code bytes)
			throws ParseException {
		bytes.insertEnter(context);

		cond.add(BC.RETI);
		bytes.add(BC.RET);
		context.events.add(new Event(new CodeRef(context, cond), new CodeRef(
				context, bytes), EventType.SET));
		System.out.println("onset " + cond.size() + " " + bytes.size());
	}

	public static void onchange(Context context, Code cond, Code bytes)
			throws ParseException {
		bytes.insertEnter(context);

		cond.add(BC.RETI);
		bytes.add(BC.RET);
		context.events.add(new Event(new CodeRef(context, cond), new CodeRef(
				context, bytes), EventType.CHANGE));
		System.out.println("onchange " + cond.size() + " " + bytes.size());
	}
}
