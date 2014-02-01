package kvv.evlang.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kvv.evlang.ParseException;
import kvv.evlang.rt.BC;

public abstract class Context {

	protected Map<String, Short> constants = new HashMap<String, Short>();

	protected Structs structs = new Structs(this);

	protected Registers registers = new Registers(this);

	protected abstract ExtRegisterDescr getExtRegisterDescr(String extRegName);

	public abstract void throwExc(String msg) throws ParseException;

	public Code initCode = new Code(this);

	{
		constants.put("INVALID", (short) 0x8000);

	}

	protected void checkROReg(RegisterDescr descr) throws ParseException {
		if (descr.readonly)
			throwExc("register is read only");
	}

	public Code codeArr = new Code(this);

	public Funcs funcDefList = new Funcs(this);

	public Func currentFunc;

	protected Set<String> names = new HashSet<String>();

	public Pool<Short> constPool = new Pool<Short>(16);
	public Pool<Short> regPool = new Pool<Short>(16);

	public Context() {
		try {
			Locals locals = new Locals();
			locals.add(new NameAndType("this", Type.NULL));
			locals.add(new NameAndType("ms", Type.INT));
			Func startFunc = new Func(this, "timer:start", locals, Type.VOID) {
				@Override
				public void compileCall(Code code) {
					code.add(BC.SETTIMER_MS);
				}
			};
			startFunc.code = new CodeRef(new Code(this));
			funcDefList.put(startFunc);

			locals = new Locals();
			locals.add(new NameAndType("this", Type.NULL));
			Func stopFunc = new Func(this, "timer:stop", locals, Type.VOID) {
				@Override
				public void compileCall(Code code) {
					code.add(BC.STOPTIMER);
				}
			};
			stopFunc.code = new CodeRef(new Code(this));
			funcDefList.put(stopFunc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Func procDecl(Type type, String name, Locals locals)
			throws ParseException {
		Func func = funcDefList.getCreateFunc(name, locals, type);
		currentFunc = func;
		return func;
	}

	protected void checkName(String name) throws ParseException {
		if (names.contains(name))
			throwExc("name '" + name + "' already defined");
		names.add(name);
	}

	public void check() throws ParseException {
		for (Func f : funcDefList.values()) {
			if (f.code == null)
				throw new ParseException("function '" + f.name
						+ "' not defined");
		}
	}

	public void buildInit() throws ParseException {
		initCode.compileRet(0);
		Func func = new Func(this, "<init>", new Locals(), Type.VOID);
		func.code = new CodeRef(initCode);
		funcDefList.setInit(func);
	}

	public static String win2utf(String str) {
		// return str;
		byte[] bytes = new byte[str.length()];
		for (int i = 0; i < str.length(); i++)
			bytes[i] = (byte) str.charAt(i);
		try {
			return new String(bytes, "Windows-1251");
		} catch (UnsupportedEncodingException e) {
			return "###";
		}
	}

}
