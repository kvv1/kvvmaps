package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

import kvv.evlang.ParseException;
import kvv.evlang.impl.LocalListDef.Local;
import kvv.evlang.rt.BC;

public class Expr {
	private final Code code = new Code();

	public final Type type;

	public Code getCode() {
		return code;
	}

	public Expr(Type type) {
		this.type = type;
		type.getSize();
	}

	public Expr(Type type, BC bc, Expr... args) {
		this.type = type;
		type.getSize();
		for (Expr arg : args)
			code.addAll(arg.getCode());
		code.add(bc);
	}

	public Expr(short val) {
		this.type = Type.INT;
		code.compileLit(val);
	}

	public Expr(Context context, String funcName, List<Expr> argList)
			throws ParseException {
		Func func = context.getFunc(funcName, argList.size());
		type = func.retType;
		type.getSize();
		if (func.retType.getSize() == 0)
			context.throwExc(funcName + " - ?");
		for (Expr c : argList)
			code.addAll(c.getCode());
		code.add(BC.CALL);
		code.add(func.n);
	}

	public Expr(Context context, String name) throws ParseException {
		Local val = context.currentFunc.locals.get(name);
		if (val != null) {
			type = val.nat.type;
			type.getSize();
			code.compileGetLocal(val.n);
		} else {
			RegisterDescr descr = context.registers.get(name);
			if (descr != null) {
				type = descr.type;
				type.getSize();
				code.compileGetreg(descr.reg);
			} else {
				Short val1 = context.constants.get(name);
				if (val1 != null) {
					type = Type.INT;
					code.compileLit(val1);
				} else {
					ExtRegisterDescr extRegisterDescr = context
							.getExtRegisterDescr(name);
					if (extRegisterDescr != null) {
						type = Type.INT;
						code.compileGetregExt(extRegisterDescr.addr,
								extRegisterDescr.reg);
					} else {
						type = Type.INT;
						context.throwExc(name + " - ?");
					}
				}
			}
		}
	}

	public static Expr muldiv(Context context, Expr e1, Expr e2, Expr e3)
			throws ParseException {
		e1.type.checkInt(context);
		e2.type.checkInt(context);
		e3.type.checkInt(context);
		return new Expr(Type.INT, BC.MULDIV, e1, e2, e3);
	}

	public static Expr not(Context context, Expr arg) throws ParseException {
		arg.type.checkInt(context);
		return new Expr(Type.INT, BC.NOT, arg);
	}

	public static Expr negate(Context context, Expr arg) throws ParseException {
		arg.type.checkInt(context);
		return new Expr(Type.INT, BC.NEGATE, arg);
	}

	public static Expr mul(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(Type.INT, BC.MUL, arg1, arg2);
	}

	public static Expr div(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(Type.INT, BC.DIV, arg1, arg2);
	}

	public static Expr add(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(Type.INT, BC.ADD, arg1, arg2);
	}

	public static Expr sub(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(Type.INT, BC.SUB, arg1, arg2);
	}

	public static Expr eq(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		Type.checkComparable(context, arg1.type, arg2.type);
		return new Expr(Type.INT, BC.EQ, arg1, arg2);
	}

	public static Expr neq(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		Type.checkComparable(context, arg1.type, arg2.type);
		return new Expr(Type.INT, BC.NEQ, arg1, arg2);
	}

	public static Expr lt(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(Type.INT, BC.LT, arg1, arg2);
	}

	public static Expr le(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(Type.INT, BC.LE, arg1, arg2);
	}

	public static Expr gt(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(Type.INT, BC.GT, arg1, arg2);
	}

	public static Expr ge(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(Type.INT, BC.GE, arg1, arg2);
	}

	public static Expr and(List<Expr> list) {
		if (list.size() == 1)
			return list.get(0);

		Expr res = new Expr(Type.INT);
		
		List<Integer> addrs = new ArrayList<Integer>();

		for (Expr e : list) {
			res.code.addAll(e.getCode());
			addrs.add(res.code.compileQBranch(0));
		}

		res.code.compileLit((short) 1);
		int a = res.code.compileBranch(0);
		res.code.resolveBranchs(addrs);
		res.code.compileLit((short) 0);
		res.code.resolveBranch(a);

		return res;
	}

	public static Expr or(List<Expr> list) {
		if (list.size() == 1)
			return list.get(0);

		Expr res = new Expr(Type.INT);
		
		List<Integer> addrs = new ArrayList<Integer>();

		for (Expr e : list) {
			res.code.addAll(e.getCode());
			res.code.add(BC.NOT);
			addrs.add(res.code.compileQBranch(0));
		}

		res.code.compileLit((short) 0);
		int a = res.code.compileBranch(0);
		res.code.resolveBranchs(addrs);
		res.code.compileLit((short) 1);
		res.code.resolveBranch(a);

		return res;
	}

	public static Expr nullExpr() {
		Expr res = new Expr(Type.NULL);
		res.code.compileLit((short) 0);
		return res;
	}
}
