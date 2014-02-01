package kvv.evlang.impl;

import kvv.evlang.ParseException;


public class Func {
	public int n;
	public CodeRef code;
	public final Type retType;
	public final String name;
	public final LocalListDef locals;

	public int maxStack = -1;

	public Func(Context context, String name, LocalListDef locals, Type retType) throws ParseException {
		this.name = name;
		this.locals = locals;
		this.retType = retType;
		locals.endOfArgs();
		
		context.checkName(name);
	}

	public void compileCall(Code code) {
		code.compileCall(n);
	}	
}