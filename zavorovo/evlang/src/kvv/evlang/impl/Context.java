package kvv.evlang.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kvv.evlang.ParseException;

public abstract class Context {

	protected Set<String> names = new HashSet<String>();

	protected Map<String, Short> constants = new HashMap<String, Short>();

	protected Structs structs = new Structs(this);

	protected Registers registers = new Registers(this);

	protected abstract ExtRegisterDescr getExtRegisterDescr(String extRegName);

	public abstract void throwExc(String msg) throws ParseException;

	{
		constants.put("INVALID", (short) 0x8000);
	}

	protected void checkROReg(RegisterDescr descr) throws ParseException {
		if (descr.readonly)
			throwExc("register is read only");
	}

	public Funcs funcs = new Funcs(this);

	public Func currentFunc;

	public Pool<Short> constPool = new Pool<Short>(16);
	public Pool<Short> regPool = new Pool<Short>(16);

	public Context() {
	}

	protected void checkName(String name) throws ParseException {
		if (names.contains(name))
			throwExc("name '" + name + "' already defined");
		names.add(name);
	}

	public void check() throws ParseException {
		for (Func f : funcs.values()) {
			if (f.code == null)
				throw new ParseException("function '" + f.name
						+ "' not defined");
		}
	}

	public void buildInit() throws ParseException {
		funcs.initFunc.code.compileRet(0);
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
