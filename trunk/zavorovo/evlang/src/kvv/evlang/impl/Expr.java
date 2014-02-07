package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

import kvv.evlang.ParseException;
import kvv.evlang.impl.Locals.Local;
import kvv.evlang.rt.BC;

public class Expr {
	// public final Context context;
	final Code code;

	public final Type type;

	public Code getCode() {
		return code;
	}

	public Expr(Context context, Type type) {
		this.type = type;
		code = new Code(context);
	}

	public Expr(Context context, Type type, int val) {
		this(context, type);
		code.compileLit((short) val);
	}

	public Expr(Context context, Type type, BC bc, Expr... args) {
		this(context, type);
		for (Expr arg : args)
			code.addAll(arg.getCode());
		code.add(bc);
	}

	public Expr(Context context, int val) {
		this(context, Type.INT, val);
	}

	public Expr(Context context, Type parentType, String funcName,
			List<Expr> argList) throws ParseException {
		Funcs funcs = context.funcs;
		if (parentType != null) {
			funcs = context.structs.get(parentType.name).funcs;
			// if (parentType.isRef()
			// && context.structs.get(parentType.name).isTimer
			// && (funcName.equals("start") || funcName.equals("stop")))
			// funcName = "timer" + ":" + funcName;
			// else
			// funcName = parentType.name + ":" + funcName;
		}
		code = new Code(context);

		List<Type> argTypes = new ArrayList<Type>();
		for (Expr a : argList)
			argTypes.add(a.type);

		Func func = funcs.getFunc(funcName, argTypes);
		type = func.retType;

		for (Expr c : argList)
			code.addAll(c.getCode());

		if (parentType != null)
			code.addAll(func.getVCallCode());
		else
			code.addAll(func.getCallCode());
	}

	public Expr(Context context, String name) throws ParseException {
		code = new Code(context);
		Local val = context.currentFunc.locals.get(name);
		if (val != null) {
			type = val.nat.type;
			code.compileGetLocal(val.n);
		} else {
			RegisterDescr descr = context.registers.get(name);
			if (descr != null) {
				type = descr.type;
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
						context.throwWatIsIt(name);
					}
				}
			}
		}
	}

	public Expr(Context context, Expr expr, String field) throws ParseException {
		code = new Code(context);
		int idx = context.structs.getFieldIndex(expr.type, field);
		type = context.structs.get(expr.type.name).fields.get(idx).type;
		code.addAll(expr.getCode());
		code.compileGetfield(idx);
	}

	public static Expr muldiv(Context context, Expr e1, Expr e2, Expr e3)
			throws ParseException {
		e1.type.checkInt(context);
		e2.type.checkInt(context);
		e3.type.checkInt(context);
		return new Expr(context, Type.INT, BC.MULDIV, e1, e2, e3);
	}

	public static Expr not(Context context, Expr arg) throws ParseException {
		arg.type.checkInt(context);
		return new Expr(context, Type.INT, BC.NOT, arg);
	}

	public static Expr negate(Context context, Expr arg) throws ParseException {
		arg.type.checkInt(context);
		return new Expr(context, Type.INT, BC.NEGATE, arg);
	}

	public static Expr mul(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(context, Type.INT, BC.MUL, arg1, arg2);
	}

	public static Expr div(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(context, Type.INT, BC.DIV, arg1, arg2);
	}

	public static Expr add(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(context, Type.INT, BC.ADD, arg1, arg2);
	}

	public static Expr sub(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(context, Type.INT, BC.SUB, arg1, arg2);
	}

	public static Expr eq(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		Type.checkComparable(context, arg1.type, arg2.type);
		return new Expr(context, Type.INT, BC.EQ, arg1, arg2);
	}

	public static Expr neq(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		Type.checkComparable(context, arg1.type, arg2.type);
		return new Expr(context, Type.INT, BC.NEQ, arg1, arg2);
	}

	public static Expr lt(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(context, Type.INT, BC.LT, arg1, arg2);
	}

	public static Expr le(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(context, Type.INT, BC.LE, arg1, arg2);
	}

	public static Expr gt(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(context, Type.INT, BC.GT, arg1, arg2);
	}

	public static Expr ge(Context context, Expr arg1, Expr arg2)
			throws ParseException {
		arg1.type.checkInt(context);
		arg2.type.checkInt(context);
		return new Expr(context, Type.INT, BC.GE, arg1, arg2);
	}

	public static Expr and(Context context, List<Expr> list) {
		if (list.size() == 1)
			return list.get(0);

		Expr res = new Expr(context, Type.INT);

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

	public static Expr or(Context context, List<Expr> list) {
		if (list.size() == 1)
			return list.get(0);

		Expr res = new Expr(context, Type.INT);

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

	public static Expr nullExpr(Context context) {
		return new Expr(context, Type.NULL, 0);
	}

	public static Expr field(Context context, Expr parent, String field)
			throws ParseException {
		return new Expr(context, parent, field);
	}

	public static Expr newObj(Context context, String typeName,
			List<Expr> argList) throws ParseException {
		Struct str = context.structs.get(typeName);

		if (str.fields.size() != argList.size())
			context.throwExc(typeName + " argument number error");

		for (int i = 0; i < argList.size(); i++)
			argList.get(i).type.checkAssignableTo(context,
					str.fields.get(i).type);

		Expr res = new Expr(context, str.type);
		for (Expr c : argList)
			res.code.addAll(c.getCode());
		res.code.add(BC.NEW);
		res.code.add(str.idx);

		str.isAbstract = false;

		return res;
	}

}
