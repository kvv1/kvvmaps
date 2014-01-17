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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.controllers.register.RegType;
import kvv.controllers.register.Register;
import kvv.controllers.register.RegisterUI;
import kvv.evlang.ParseException;
import kvv.evlang.impl.Event.EventType;
import kvv.evlang.rt.BC;
import kvv.evlang.rt.RTContext;
import kvv.evlang.rt.TryCatchBlock;
import kvv.evlang.rt.UncaughtExceptionException;
import kvv.evlang.rt.VM;

public abstract class Context {

	protected Map<String, Struct> structs = new HashMap<String, Struct>();

	protected Map<String, Short> constants = new HashMap<String, Short>();

	protected Map<String, RegisterDescr> registers = new LinkedHashMap<String, RegisterDescr>();

	protected int nextReg = Register.REG_RAM0;
	protected int nextEEReg = Register.REG_EEPROM0;

	protected abstract ExtRegisterDescr getExtRegisterDescr(String extRegName);

	public abstract void throwExc(String msg) throws ParseException;

	public static PrintStream nullStream = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}
	});

	public PrintStream dumpStream = nullStream;

	{
		constants.put("INVALID", (short) 0x8000);

		registers.put("REG_RELAY0", new RegisterDescr(Type.INT,
				Register.REG_RELAY0));
		registers.put("REG_RELAY1", new RegisterDescr(Type.INT,
				Register.REG_RELAY1));
		registers.put("REG_RELAY2", new RegisterDescr(Type.INT,
				Register.REG_RELAY2));
		registers.put("REG_RELAY3", new RegisterDescr(Type.INT,
				Register.REG_RELAY3));
		registers.put("REG_RELAY4", new RegisterDescr(Type.INT,
				Register.REG_RELAY4));
		registers.put("REG_RELAY5", new RegisterDescr(Type.INT,
				Register.REG_RELAY5));
		registers.put("REG_RELAY6", new RegisterDescr(Type.INT,
				Register.REG_RELAY6));
		registers.put("REG_RELAY7", new RegisterDescr(Type.INT,
				Register.REG_RELAY7));
		registers.put("REG_TEMPERATURE", new RegisterDescr(Type.INT,
				Register.REG_TEMP, true, false));
		registers.put("REG_TEMPERATURE2", new RegisterDescr(Type.INT,
				Register.REG_TEMP2, true, false));

		// registers.put("REG_EEPROM0", Register.REG_EEPROM0);
		// registers.put("REG_EEPROM1", Register.REG_EEPROM1);
		// registers.put("REG_EEPROM2", Register.REG_EEPROM2);
		// registers.put("REG_EEPROM3", Register.REG_EEPROM3);
		// registers.put("REG_EEPROM4", Register.REG_EEPROM4);
		// registers.put("REG_EEPROM5", Register.REG_EEPROM5);
		// registers.put("REG_EEPROM6", Register.REG_EEPROM6);
		// registers.put("REG_EEPROM7", Register.REG_EEPROM7);

		registers.put("REG_ADC0", new RegisterDescr(Type.INT,
				Register.REG_ADC0, true, false));
		registers.put("REG_ADC1", new RegisterDescr(Type.INT,
				Register.REG_ADC1, true, false));
		registers.put("REG_ADC2", new RegisterDescr(Type.INT,
				Register.REG_ADC2, true, false));
		registers.put("REG_ADC3", new RegisterDescr(Type.INT,
				Register.REG_ADC3, true, false));

		registers.put("REG_IN0", new RegisterDescr(Type.INT, Register.REG_IN0,
				true, false));
		registers.put("REG_IN1", new RegisterDescr(Type.INT, Register.REG_IN1,
				true, false));
		registers.put("REG_IN2", new RegisterDescr(Type.INT, Register.REG_IN2,
				true, false));
		registers.put("REG_IN3", new RegisterDescr(Type.INT, Register.REG_IN3,
				true, false));
		registers.put("REG_IN4", new RegisterDescr(Type.INT, Register.REG_IN4,
				true, false));
		registers.put("REG_IN5", new RegisterDescr(Type.INT, Register.REG_IN5,
				true, false));
		registers.put("REG_IN6", new RegisterDescr(Type.INT, Register.REG_IN6,
				true, false));
		registers.put("REG_IN7", new RegisterDescr(Type.INT, Register.REG_IN7,
				true, false));

		registers.put("REG_INPULLUP0", new RegisterDescr(Type.INT,
				Register.REG_INPULLUP0));
		registers.put("REG_INPULLUP1", new RegisterDescr(Type.INT,
				Register.REG_INPULLUP1));
		registers.put("REG_INPULLUP2", new RegisterDescr(Type.INT,
				Register.REG_INPULLUP2));
		registers.put("REG_INPULLUP3", new RegisterDescr(Type.INT,
				Register.REG_INPULLUP3));
		registers.put("REG_INPULLUP4", new RegisterDescr(Type.INT,
				Register.REG_INPULLUP4));
		registers.put("REG_INPULLUP5", new RegisterDescr(Type.INT,
				Register.REG_INPULLUP5));
		registers.put("REG_INPULLUP6", new RegisterDescr(Type.INT,
				Register.REG_INPULLUP6));
		registers.put("REG_INPULLUP7", new RegisterDescr(Type.INT,
				Register.REG_INPULLUP7));

	}

	protected List<RegisterUI> registerUIs = new ArrayList<RegisterUI>();

	protected void checkROReg(RegisterDescr descr) throws ParseException {
		if (descr.readonly)
			throwExc("register is read only");
	}

	public Func currentFunc;
	// public LocalListDef locals = new LocalListDef();
	// public boolean isFunc;

	public Code codeArr = new Code();

	public FuncDefList funcDefList = new FuncDefList();

	public Func getFunc(String name, List<Expr> argList) throws ParseException {
		Func func = funcDefList.get(name);
		if (func == null)
			throwExc(name + " - ?");
		
		if (func.locals.getArgCnt() != argList.size())
			throwExc(name + " argument number error");
		
		for(int i = 0; i < func.locals.getArgCnt(); i++)
			argList.get(i).type.checkAssignableTo(this, func.locals.get(i).nat.type);
			
		return func;
	}

	public Func getCreateFunc(String name, LocalListDef locals, Type retType)
			throws ParseException {
		Func func = funcDefList.get(name);
		if (func == null) {
			checkName(name);
			func = new Func(this, name, locals, retType);
			funcDefList.put(func);
		} else if (!func.retType.equals(retType)) {
			throwExc(name + " - ?");
		} else if (func.locals.getArgCnt() != locals.getArgCnt())
			throwExc(name + " argument number error");
		
		for(int i = 0; i < func.locals.getArgCnt(); i++)
			locals.get(i).nat.type.checkAssignableTo(this, func.locals.get(i).nat.type);
		
		return func;
	}

	protected void createStruct(String name) throws ParseException {
		checkName(name);
		structs.put(name, new Struct(name));
	}

	protected void addField(String structName, Type fieldType,
			String fieldName) throws ParseException {
		Struct struct = structs.get(structName);
		if (struct == null)
			throwExc(structName + " -?");

		if (struct.fields.containsKey(fieldName))
			throwExc(fieldName + " already defined");

		struct.fields.put(fieldName, fieldType);
	}

	protected void newRegisterAlias(String regName, String regNum)
			throws ParseException {
		checkName(regName);
		RegisterDescr registerDescr = registers.get(regNum);
		if (registerDescr == null)
			throwExc(regNum + " - ?");
		registers.put(regName, registerDescr);
	}

	protected void newRegister(Type type, String regName) throws ParseException {
		checkName(regName);
		RegisterDescr registerDescr = new RegisterDescr(type, nextReg++, false,
				false, null);
		if (nextReg > Register.REG_RAM0 + Register.REG_RAM_CNT)
			throwExc("too many registers used");
		registers.put(regName, registerDescr);
	}

	protected void newEERegister(String regName, Short initValue)
			throws ParseException {
		checkName(regName);
		RegisterDescr registerDescr = new RegisterDescr(Type.INT, nextEEReg++,
				true, true, initValue);
		if (nextEEReg > Register.REG_EEPROM0 + Register.REG_EEPROM_CNT)
			throwExc("too many registers used");
		registers.put(regName, registerDescr);
	}

	protected void setUI(String regName, String uiName, RegType uiType)
			throws ParseException {
		RegisterDescr registerDescr = registers.get(regName);
		if (registerDescr == null)
			throwExc(regName + " - ?");

		if (uiType == RegType.textRW && !registerDescr.editable)
			uiType = RegType.textRO;

		registerUIs.add(new RegisterUI(registerDescr.reg, uiType, uiName));
	}

	public Collection<Event> events = new ArrayList<Event>();

	protected Map<String, Timer> timers = new LinkedHashMap<String, Timer>();
	protected int nextTimer = 0;

	protected Timer getCreateTimer(String name) throws ParseException {
		Timer timer = timers.get(name);
		if (timer == null) {
			checkName(name);
			timer = new Timer(this, name, nextTimer++);
			timers.put(name, timer);
		}
		return timer;
	}

	protected Set<String> names = new HashSet<String>();

	protected void checkName(String name) throws ParseException {
		if (names.contains(name))
			throwExc("name '" + name + "' already defined");
		names.add(name);
	}

	public void check() throws ParseException {
		for (Timer t : timers.values()) {
			if (t.handler == null)
				throw new ParseException("timer '" + t.name + "' not defined");
		}

		for (Func f : funcDefList.values()) {
			if (f.code == null)
				throw new ParseException("function '" + f.name
						+ "' not defined");
		}

	}

	public void buildInit() {
		Code code = new Code();

		for (RegisterDescr reg : registers.values()) {
			if (reg.initValue != null) {
				code.compileLit(reg.initValue);
				code.compileSetreg(reg.reg);
			}
		}

		code.add(BC.RET);

		Func func = new Func(this, "<init>", new LocalListDef(), Type.VOID);
		func.code = new CodeRef(this, code);
		funcDefList.setInit(func);
	}

	/*
	 * public void checkStack() throws ParseException { int maxStack = 0;
	 * 
	 * for (Timer t : timers.values()) { int stack = t.getMaxStack(); maxStack =
	 * Math.max(maxStack, stack); }
	 * 
	 * for (Func f : funcDefList.values()) { int stack = f.getMaxStack();
	 * maxStack = Math.max(maxStack, stack); }
	 * 
	 * for (Event e : events) { int stack = e.getCondMaxStack(); maxStack =
	 * Math.max(maxStack, stack); stack = e.getHandlerMaxStack(); maxStack =
	 * Math.max(maxStack, stack); } System.out.println("maxstack = " +
	 * maxStack); }
	 */
	// public static byte[] dumpNull() throws IOException {
	// return new byte[0];
	// }

	public byte[] dump() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		RegisterUI[] regs = getRegisterDescriptions();
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

		dos.writeByte(timers.size());
		for (Timer t : timers.values()) {
			dos.writeShort(t.handler.off);
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

		RTContext.Timer rtTimers[] = new RTContext.Timer[timers.size()];
		for (Timer t : timers.values())
			rtTimers[t.n] = new RTContext.Timer(t.handler.off);

		RTContext.Func rtFuncs[] = new RTContext.Func[funcDefList.size()];
		for (Func f : funcDefList.values())
			rtFuncs[f.n] = new RTContext.Func(f.code.off);

		RTContext context = new RTContext(codeArr.code, rtTimers,
				rtEvents.toArray(new RTContext.Event[0]), rtFuncs,
				codeArr.tryCatchBlocks.toArray(new TryCatchBlock[0]));

		return context;
	}

	public RegisterUI[] getRegisterDescriptions() {
		return registerUIs.toArray(new RegisterUI[0]);
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

}
