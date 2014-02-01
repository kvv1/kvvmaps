package kvv.evlang.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.controllers.register.RegisterUI;
import kvv.evlang.ParseException;
import kvv.evlang.impl.Event.EventType;
import kvv.evlang.rt.BC;
import kvv.evlang.rt.RTContext;
import kvv.evlang.rt.TryCatchBlock;
import kvv.evlang.rt.UncaughtExceptionException;
import kvv.evlang.rt.VM;

public abstract class Context {

	private static final int MAX_CONSTPOOL = 16;
	private static final int MAX_REGPOOL = 16;

	protected Map<String, Short> constants = new HashMap<String, Short>();
	
	protected Structs structs = new Structs(this);

	protected Registers registers = new Registers(this);

	protected abstract ExtRegisterDescr getExtRegisterDescr(String extRegName);

	public abstract void throwExc(String msg) throws ParseException;

	public static PrintStream nullStream = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}
	});

	public Code initCode = new Code(this);

	public PrintStream dumpStream = nullStream;

	{
		constants.put("INVALID", (short) 0x8000);

	}

	protected void checkROReg(RegisterDescr descr) throws ParseException {
		if (descr.readonly)
			throwExc("register is read only");
	}

	public Code codeArr = new Code(this);

	public FuncDefList funcDefList = new FuncDefList(this);

	public Func currentFunc;

	public Context() {
		try {
			LocalListDef locals = new LocalListDef();
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

			locals = new LocalListDef();
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

	public Func procDecl(Type type, String name,
			LocalListDef locals) throws ParseException {
		Func func = funcDefList.getCreateFunc(name, locals, type);
		currentFunc = func;
		return func;
	}

	public Collection<Event> events = new ArrayList<Event>();

	protected Set<String> names = new HashSet<String>();

	public List<Short> constPool = new ArrayList<Short>();
	public List<Short> regPool = new ArrayList<Short>();

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
		Func func = new Func(this, "<init>", new LocalListDef(), Type.VOID);
		func.code = new CodeRef(initCode);
		funcDefList.setInit(func);
	}

	public byte[] dump() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		RegisterUI[] regs = registers.getRegisterDescriptions();
		dos.writeByte(regs.length);
		for (RegisterUI reg : regs) {
			dos.writeByte(reg.reg);
			dos.writeByte(reg.type.ordinal());
			dos.writeByte(reg.text.length());
			dos.writeBytes(reg.text);
		}

		dos.writeByte(events.size());
		for (Event e : events) {
			dos.writeShort(e.cond.off
					| (e.type == EventType.CHANGE ? 0x8000 : 0));
			dos.writeShort(e.handler.off);
		}

		dos.writeByte(funcDefList.size());
		for (Func f : funcDefList.values()) {
			dos.writeShort(f.code.off);
		}

		dos.writeByte(codeArr.tryCatchBlocks.size());
		for (TryCatchBlock tcb : codeArr.tryCatchBlocks) {
			dos.writeShort(tcb.from);
			dos.writeShort(tcb.to);
			dos.writeShort(tcb.handler);
		}

		dos.writeByte(constPool.size());
		for (Short s : constPool) {
			dos.writeShort(s);
		}

		dos.writeByte(regPool.size());
		for (Short s : regPool) {
			dos.writeByte(s);
		}

		for (byte b : codeArr.code)
			dos.write(b);

		dos.close();
		return baos.toByteArray();
	}

	public void run() throws UncaughtExceptionException {
		RTContext context = getRTContext();
		new VM(context) {
			@Override
			public void setExtReg(int addr, int reg, int value) {
			}

			@Override
			public int getExtReg(int addr, int reg) {
				return 0;
			}
		}.loop();
	}

	public RTContext getRTContext() {
		Collection<RTContext.Event> rtEvents = new ArrayList<RTContext.Event>();
		for (Event e : events)
			rtEvents.add(new RTContext.Event(e.cond.off, e.handler.off, e.type
					.ordinal()));

		RTContext.Func rtFuncs[] = new RTContext.Func[funcDefList.size()];
		for (Func f : funcDefList.values())
			rtFuncs[f.n] = new RTContext.Func(f.code.off);

		RTContext.Type[] rtTypes = new RTContext.Type[structs.size()];
		for (Struct str : structs.values())
			rtTypes[str.idx] = new RTContext.Type(str.fields.size(),
					str.getMask());

		RTContext context = new RTContext(codeArr.code,
				rtEvents.toArray(new RTContext.Event[0]), rtFuncs,
				codeArr.tryCatchBlocks.toArray(new TryCatchBlock[0]),
				constPool.toArray(new Short[0]), regPool.toArray(new Short[0]),
				registers.refs.toArray(new Byte[0]), rtTypes);

		return context;
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

	public void checkNotVoid(Type type) throws ParseException {
		if (type.equals(Type.VOID))
			throwExc("'void' type not allowed");
	}

	public Integer addConst(short s) {
		for (int i = 0; i < constPool.size(); i++)
			if (constPool.get(i) == s)
				return i;
		if (constPool.size() < MAX_CONSTPOOL) {
			constPool.add(s);
			return constPool.size() - 1;
		}
		return null;
	}

	public Integer addReg(short s) {
		for (int i = 0; i < regPool.size(); i++)
			if (regPool.get(i) == s)
				return i;
		if (regPool.size() < MAX_REGPOOL) {
			regPool.add(s);
			return regPool.size() - 1;
		}
		return null;
	}

}
