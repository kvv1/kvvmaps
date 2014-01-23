package kvv.evlang.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kvv.evlang.ParseException;
import kvv.evlang.impl.Event.EventType;
import kvv.evlang.impl.LocalListDef.Local;
import kvv.evlang.rt.BC;
import kvv.evlang.rt.TryCatchBlock;

public class Code extends CodeBase {

	public Code(Context context) {
		super(context);
	}

	public Set<TryCatchBlock> tryCatchBlocks = new HashSet<TryCatchBlock>();

	public void addAll(Code c) {
		short sz = size();
		code.addAll(c.code);
		for (TryCatchBlock tcb : c.tryCatchBlocks)
			tryCatchBlocks.add(new TryCatchBlock(tcb.from + sz, tcb.to + sz,
					tcb.handler + sz));
		for (int local : c.locals)
			locals.add(local + sz);
	}

	public static Code dec(Context context, String name) throws ParseException {
		RegisterDescr descr = context.registers.get(name);
		if (descr == null)
			context.throwExc(name + " - ?");
		context.checkROReg(descr);
		Code res = new Code(context);
		res.add(BC.DEC);
		res.add(descr.reg);
		return res;
	}

	public static Code inc(Context context, String name) throws ParseException {
		RegisterDescr descr = context.registers.get(name);
		if (descr == null)
			context.throwExc(name + " - ?");
		context.checkROReg(descr);
		Code res = new Code(context);
		res.add(BC.INC);
		res.add(descr.reg);
		return res;
	}

//	public static Code callp(Context context, LValue lvalue, List<Expr> argList)
//			throws ParseException {
//		if (lvalue.expr != null)
//			context.throwExc("method calls not implemented");
//		Func func = context.getFunc(lvalue.field, argList);
//		Code res = new Code(context);
//		for (Expr c : argList)
//			res.addAll(c.getCode());
//		res.compileCall(func.n);
//		if (func.retType.getSize() != 0)
//			res.add(BC.DROP);
//		return res;
//	}

	public static Code stop(Context context, String name) throws ParseException {
		Timer timer = context.getCreateTimer(name);
		Code res = new Code(context);
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
			bytes.compileRetI(locals);
			return bytes;
		} else {
			if (context.currentFunc.retType.getSize() != 0)
				context.throwExc("'return <expr>;' expected");
			Code bytes = new Code(context);
			int locals = context.currentFunc.locals.getMax();
			bytes.compileRet(locals);
			return bytes;
		}
	}

	public static Code throw_(Context context, Expr n) {
		Code res = n.getCode();
		res.add(BC.THROW);
		return res;
	}

	public static Code assign(Context context, LValue lvalue, Expr t)
			throws ParseException {
		if (lvalue.expr == null)
			return assign(context, lvalue.field, t);
		else {
			if(t == null) {
				Expr e = lvalue.getExpr();
				Code code = lvalue.getExpr().getCode();
				if(e.type != Type.VOID)
					code.add(BC.DROP);
				return code;
			}
			
			int idx = context.getFieldIndex(lvalue.expr.type, lvalue.field);
			Type type = context.structs.get(lvalue.expr.type.name).fields
					.get(idx).type;
			t.type.checkAssignableTo(context, type);

			Code res = new Code(context);
			res.addAll(lvalue.expr.getCode());
			res.addAll(t.getCode());
			res.compileSetfield(idx);
			return res;
		}
	}

	private static Code assign(Context context, String name, Expr t)
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
			Code catchStmt, Local local) throws ParseException {
		Code res = new Code(context);

		int from = res.size();
		res.addAll(tryStmt);
		int to = res.size();
		int b = res.compileBranch(0);
		int handler = res.size();
		res.compileSetLocal(local.n);
		res.addAll(catchStmt);
		res.resolveBranch(b);

		res.tryCatchBlocks.add(new TryCatchBlock(from, to, handler));

		return res;
	}

	public static Code initLocal(Context context, Local local, Expr initVal) throws ParseException {
		initVal.type.checkAssignableTo(context, local.nat.type);
		
		Code res = new Code(context);
		res.addAll(initVal.code);
		res.compileSetLocal(local.n);
		return res;
	}
	
	public static Func procDecl(Context context, Type type, String name,
			LocalListDef locals) throws ParseException {
		Func func = context.getCreateFunc(name, locals, type);
		context.currentFunc = func;
		return func;
	}

	private void compileEnter() throws ParseException {
		compileEnter(context.currentFunc.locals.getMax()
				- context.currentFunc.locals.getArgCnt());
	}

	public static void procCode(Context context, Code bytes)
			throws ParseException {

		Code res = new Code(context);
		res.compileEnter();
		res.addAll(bytes);
		if (context.currentFunc.retType.getSize() == 0)
			res.addAll(ret(context, null));
		else if (context.currentFunc.retType.isRef())
			res.addAll(ret(context, Expr.nullExpr(context)));
		else
			res.addAll(ret(context, new Expr(context, (short) 0)));
		res.adjustLocals();

		context.currentFunc.code = new CodeRef(context, res);
		System.out.println("proc " + context.currentFunc.name + " "
				+ res.size());
	}

	public static void timer(Context context, String name, Code bytes)
			throws ParseException {
		Code res = new Code(context);
		res.compileEnter();
		res.addAll(bytes);
		res.compileRet(0);
		res.adjustLocals();

		Timer timer = context.getCreateTimer(name);
		timer.handler = new CodeRef(context, res);
		System.out.println("timer " + name + " " + res.size());
	}

	public static void onset(Context context, Code cond, Code bytes)
			throws ParseException {
		Code res = new Code(context);
		res.compileEnter();
		res.addAll(bytes);
		res.compileRet(0);
		res.adjustLocals();

		cond.compileRetI(0);
		context.events.add(new Event(context, new CodeRef(context, cond),
				new CodeRef(context, res), EventType.SET));
		System.out.println("onset " + cond.size() + " " + res.size());
	}

	public static void onchange(Context context, Code cond, Code bytes)
			throws ParseException {
		Code res = new Code(context);
		res.compileEnter();
		res.addAll(bytes);
		res.compileRet(0);
		res.adjustLocals();

		cond.compileRetI(0);
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
