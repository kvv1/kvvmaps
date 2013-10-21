package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

import kvv.evlang.ParseException;
import kvv.evlang.rt.BC;

public class Expr {
	private final Code code = new Code();

	public Code getCode() {
		return code;
	}

	public Expr(Code code) {
		this.code.addAll(code);
	}

	public Expr(BC bc, Expr... args) {
		for (Expr arg : args)
			code.addAll(arg.getCode());
		code.add(bc);
	}

	public Expr(Short val) {
		code.compileLit(val);
	}

	public Expr(Context context, String funcName, List<Expr> argList)
			throws ParseException {
		Func func = context.getFunc(funcName, argList.size());
		if (func.retSize != 1)
			context.throwExc(funcName + " - ?");
		for (Expr c : argList)
			code.addAll(c.getCode());
		code.add(BC.CALL);
		code.add(func.n);
	}

	public Expr(Context context, String name) throws ParseException {
		Integer val = context.currentFunc.locals.get(name);
		if (val != null) {
			code.compileGetLocal(context.currentFunc.locals.getArgCnt() - val
					- 1);
		} else {
			RegisterDescr descr = context.registers.get(name);
			if (descr != null) {
				code.compileGetreg(descr.reg);
			} else {
				Short val1 = context.constants.get(name);
				if (val1 != null) {
					code.compileLit(val1);
				} else {
					ExtRegisterDescr extRegisterDescr = context
							.getExtRegisterDescr(name);
					if (extRegisterDescr != null) {
						code.compileGetregExt(extRegisterDescr.addr,
								extRegisterDescr.reg);
					} else {
						context.throwExc(name + " - ?");
					}
				}
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

	public static Expr and(List<Expr> list) {
		if (list.size() == 1)
			return list.get(0);

		Code code = new Code();

		List<Integer> addrs = new ArrayList<Integer>();

		for (Expr e : list) {
			code.addAll(e.getCode());
			code.add(BC.QBRANCH);
			code.add(0);
			addrs.add(code.code.size());
		}

		code.compileLit((short) 1);
		code.add(BC.BRANCH);
		code.add(0);
		int a = code.code.size();
		code.resolveBranchs(addrs);
		code.compileLit((short) 0);
		code.resolveBranch(a);

		return new Expr(code);
	}

	public static Expr or(List<Expr> list) {
		if (list.size() == 1)
			return list.get(0);

		Code code = new Code();

		List<Integer> addrs = new ArrayList<Integer>();

		for (Expr e : list) {
			code.addAll(e.getCode());
			code.add(BC.NOT);
			code.add(BC.QBRANCH);
			code.add(0);
			addrs.add(code.code.size());
		}

		code.compileLit((short) 0);
		code.add(BC.BRANCH);
		code.add(0);
		int a = code.code.size();
		code.resolveBranchs(addrs);
		code.compileLit((short) 1);
		code.resolveBranch(a);

		return new Expr(code);
	}

}
