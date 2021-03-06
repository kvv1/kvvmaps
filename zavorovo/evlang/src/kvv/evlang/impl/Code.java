package kvv.evlang.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kvv.evlang.ParseException;
import kvv.evlang.impl.Locals.Local;
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
			context.throwWatIsIt(name);
		context.checkROReg(descr);
		Code res = new Code(context);
		res.add(BC.DEC);
		res.addN(descr.reg);
		return res;
	}

	public static Code inc(Context context, String name) throws ParseException {
		RegisterDescr descr = context.registers.get(name);
		if (descr == null)
			context.throwWatIsIt(name);
		context.checkROReg(descr);
		Code res = new Code(context);
		res.add(BC.INC);
		res.addN(descr.reg);
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

	public static Code assignField(Context context, Expr obj, String field,
			Expr val) throws ParseException {
		int idx = context.structs.getFieldIndex(obj.type, field);
		Type type = context.structs.get(obj.type.name).fields.get(idx).type;
		val.type.checkAssignableTo(context, type);

		Code res = new Code(context);
		res.addAll(obj.getCode());
		res.addAll(val.getCode());
		res.compileSetfield(idx);
		return res;
	}

	public static Code assignIndex(Context context, Expr expr, Expr index, Expr t) throws ParseException {
		index.type.checkInt(context);
		t.type.checkAssignableTo(context, new Type(expr.type, expr.type.arrayLevel - 1));
		
		Code res = new Code(context);
		res.addAll(expr.getCode());
		res.addAll(index.getCode());
		res.addAll(t.getCode());
		res.compileSetArray();
		return res;
	}

	public static Code assignVar(Context context, String name, Expr t)
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
					context.throwWatIsIt(name);
				}
			}
		}
		return res;
	}

	public int compileBranch(BC bc, int... off) {
		add(bc);
		for (int n : off)
			addN(n);
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

	public static Code whilestmt(Expr cond, Code stmt) {
		Code res = cond.getCode();
		int a = res.compileQBranch(0);
		res.addAll(stmt);
		int b = res.compileBranch(0);
		res.resolveBranch(b, 0);
		res.resolveBranch(a);
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

	public static Code initLocal(Context context, Local local, Expr initVal)
			throws ParseException {
		initVal.type.checkAssignableTo(context, local.nat.type);

		Code res = new Code(context);
		res.addAll(initVal.code);
		res.compileSetLocal(local.n);
		return res;
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
			res.addAll(ret(context, new Expr(context, 0)));
		res.adjustLocals();

		if (context.currentFunc.code != null)
			context.throwAlreadyDefined(context.currentFunc.name);

		context.currentFunc.code = res;
		System.out.println("proc " + context.currentFunc.name + " "
				+ res.size());
	}

	public void resolveBranchs(List<Integer> addrs) {
		for (int a : addrs)
			resolveBranch(a);
	}

	public void resolveBranch(int cmdAddr, int dest) {
		code.set(cmdAddr - 1, (byte) (dest - cmdAddr));
	}

	public void resolveBranch(int cmdAddr) {
		code.set(cmdAddr - 1, (byte) (code.size() - cmdAddr));
	}

	public static Code trap(Context context) {
		Code res = new Code(context);
		res.add(BC.TRAP);
		return res;
	}

}
