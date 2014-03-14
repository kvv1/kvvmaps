package kvv.evlang.impl;

import java.io.IOException;
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

	protected Registers registers;

	protected abstract ExtRegisterDescr getExtRegisterDescr(String extRegName);

	public abstract void throwExc(String msg) throws ParseException;

	{
		constants.put("INVALID", (short) 0x8000);
	}

	protected void checkROReg(RegisterDescr descr) throws ParseException {
		if (descr.readonly)
			throwExc("register is read only");
	}

	public Funcs funcs = new Funcs(this, true);

	public Func currentFunc;

	public Pool<Short> constPool = new Pool<Short>(16);
	public Pool<Short> regPool = new Pool<Short>(16);

	public Code code;

	public void init(String controllerType) throws IOException {
		registers = new Registers(this, controllerType);
		try {
			declareStruct("Timer");
			createStruct("Timer", null);
			closeStruct("Timer");

			declareStruct("Trigger");
			createStruct("Trigger", null);
			closeStruct("Trigger");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	protected void checkName(String name) throws ParseException {
		if (names.contains(name))
			throwAlreadyDefined(name);
		names.add(name);
	}

	public void check() throws ParseException {
		for (Func f : funcs.values()) {
			if (!f.isDefined())
				throw new ParseException("function '" + f.name
						+ "' not defined");
		}
		for (Struct str : structs.values()) {
			if (!str.isAbstract()) {
				for (Func f : str.funcs.values()) {
					if (!f.isDefined())
						throw new ParseException("function '" + str.type.name
								+ ":" + f.name + "' not defined");
				}
			}
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

	public void throwWatIsIt(String txt) throws ParseException {
		throwExc(txt + " - ?");
	}

	protected void genCode() {
		code = new Code(this);
		funcs.dump(code);
		for (Struct str : structs.values())
			if (str.isCreated())
				str.funcs.dump(code);
	}

	public void throwAlreadyDefined(String string) throws ParseException {
		throwExc(string + " already defined");
	}

	public void throwArgNumErr(String string) throws ParseException {
		throwExc(string + " argument number error");
	}

	public void throwShouldBeDefined(String string) throws ParseException {
		throwExc(string + " should be defined");
	}

	// /////////////////////////////////

	public void declareStruct(String name) throws ParseException {
		structs.createStruct(name);
	}

	public void createStruct(String name, String superName)
			throws ParseException {
		structs.create(name, superName);
	}

	public void closeStruct(String name) throws ParseException {
		structs.close(name);

	}

	public void declareFunc(Type type, Type classType, String name,
			Locals argList) throws ParseException {
		if (classType == null)
			funcs.getCreateFunc(type, name, argList, false);
		else {
			Struct str = structs.get(classType.name);
			if (!str.isCreated())
				throwShouldBeDefined(classType.name);
			str.funcs.getCreateFunc(type, name, argList, str.closed);
		}
	}

	public void createField(String structName, Type type, String fieldName)
			throws ParseException {
		Struct str = structs.get(structName);
		if (!str.isCreated())
			throwShouldBeDefined(structName);
		str.addField(type, fieldName);
	}
}
