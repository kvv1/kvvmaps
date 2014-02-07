package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Func {
	public int n;
	public Code code;
	public final Type retType;
	public final String name;
	public final Locals locals;

	private short off;

	public final Func superFunc;

	private final Context context;

	public Func(Context context, String name, Locals locals, Type retType)
			throws ParseException {
		this.context = context;
		this.name = name;
		this.locals = locals;
		this.retType = retType;
		superFunc = null;
		locals.endOfArgs();
	}

	public Func(Func superFunc, Type thisType) {
		this.context = superFunc.context;
		this.superFunc = superFunc;
		this.n = superFunc.n;
		this.retType = superFunc.retType;
		this.name = superFunc.name;
		this.locals = new Locals(superFunc.locals, thisType);
	}

	public void dump(Code code) {
		if (this.code != null) {
			off = code.size();
			code.addAll(this.code);
		}
	}

	public Code getCallCode() {
		Code code = new Code(context);
		code.compileCall(n);
		return code;
	}

	public Code getVCallCode() {
		if (superFunc != null)
			return superFunc.getVCallCode();
		Code code = new Code(context);
		code.compileVCall(locals.getArgCnt(), n);
		return code;
	}

	public boolean isDefined() {
		if (code != null)
			return true;
		if (superFunc == null)
			return false;
		return superFunc.isDefined();
	}

	public short getOff() {
		if (code != null)
			return off;
		if (superFunc != null)
			return superFunc.getOff();
		throw new IllegalStateException();
	}

	public void print() {
		System.out.println(n + " " + name + " "
				+ (code == null ? "null" : off + " " + code.size()));
	}
}