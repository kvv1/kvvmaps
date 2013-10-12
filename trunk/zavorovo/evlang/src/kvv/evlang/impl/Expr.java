package kvv.evlang.impl;

import java.util.List;

import kvv.evlang.ParseException;

public class Expr {
	private Code code;

	// private Short val;

	public Expr(BC bc, Expr... args) {
		code = new Code();
		for (Expr arg : args)
			code.addAll(arg.getCode());
		code.add(bc);
	}

	public Expr(Short val) {
		code = new Code();
		code.compileLit(val);
	}

	public Expr(Context context, String funcName, List<Expr> argList)
			throws ParseException {
		Func func = context.getFunc(funcName, argList.size());
		if (func.retSize != 1)
			throw new ParseException(funcName + " - ?");
		code = new Code();
		for (Expr c : argList)
			code.addAll(c.getCode());
		code.add(BC.CALL);
		code.add(func.n);
	}

	public Code getCode() {
		return code;
	}

	public Expr(Context context, String name) throws ParseException {
		Integer val = context.currentFunc.locals.get(name);
		if (val != null) {
			code = new Code();
			code.compileGetLocal(context.currentFunc.locals.getArgCnt() - val
					- 1);
		} else {
			RegisterDescr descr = context.registers.get(name);
			if (descr != null) {
				code = new Code();
				code.compileGetreg(descr.reg);
			} else {
				Short val1 = context.constants.get(name);
				if (val1 == null)
					throw new ParseException(name + " - ?");
				code = new Code();
				code.compileLit(val1);
			}
		}
	}

	public static Expr muldiv(Expr e1, Expr e2, Expr e3) {
		return new Expr(BC.MULDIV, e1, e2, e3);
	}

	public static Expr not(Expr arg) {
		return new Expr(BC.NOT, arg);
	}

	public static Expr negate(Expr arg) {
		return new Expr(BC.NEGATE, arg);
	}

	public static Expr mul(Expr arg1, Expr arg2) {
		return new Expr(BC.MUL, arg1, arg2);
	}

	public static Expr div(Expr arg1, Expr arg2) {
		return new Expr(BC.DIV, arg1, arg2);
	}

	public static Expr add(Expr arg1, Expr arg2) {
		return new Expr(BC.ADD, arg1, arg2);
	}

	public static Expr sub(Expr arg1, Expr arg2) {
		return new Expr(BC.SUB, arg1, arg2);
	}

	public static Expr eq(Expr arg1, Expr arg2) {
		return new Expr(BC.EQ, arg1, arg2);
	}

	public static Expr neq(Expr arg1, Expr arg2) {
		return new Expr(BC.NEQ, arg1, arg2);
	}

	public static Expr lt(Expr arg1, Expr arg2) {
		return new Expr(BC.LT, arg1, arg2);
	}

	public static Expr le(Expr arg1, Expr arg2) {
		return new Expr(BC.LE, arg1, arg2);
	}

	public static Expr gt(Expr arg1, Expr arg2) {
		return new Expr(BC.GT, arg1, arg2);
	}

	public static Expr ge(Expr arg1, Expr arg2) {
		return new Expr(BC.GE, arg1, arg2);
	}

	public static Expr and(Expr arg1, Expr arg2) {
		return new Expr(BC.AND, arg1, arg2);
	}

	public static Expr or(Expr arg1, Expr arg2) {
		return new Expr(BC.OR, arg1, arg2);
	}
}
