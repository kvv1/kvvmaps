package kvv.evlang.impl;

import kvv.evlang.ParseException;


public class Func {
	public int n;
	public Code code;
	public final Type retType;
	public final String name;
	public final Locals locals;

	public int maxStack = -1;
	public short off;

	public Func(Context context, String name, Locals locals, Type retType) throws ParseException {
		this.name = name;
		this.locals = locals;
		this.retType = retType;
		locals.endOfArgs();
	}

	public void compileCall(Code code) {
		code.compileCall(n);
	}

	public void dump(Code code) {
		off = code.size();
		code.addAll(this.code);
	}	
}