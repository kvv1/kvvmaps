package kvv.evlang.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
import kvv.controllers.register.RegisterDescr;
import kvv.controllers.register.RegisterUI;
import kvv.evlang.ParseException;
import kvv.evlang.impl.Event.EventType;
import kvv.evlang.rt.RTContext;
import kvv.evlang.rt.VM;

public class Context {
	protected Map<String, Short> constants = new HashMap<String, Short>();

	protected Map<String, RegisterDescr> registers = new LinkedHashMap<String, RegisterDescr>();
	protected int nextReg = Register.REG_RAM0;
	protected int nextEEReg = Register.REG_EEPROM0;

	{
		registers.put("REG_RELAY0", new RegisterDescr(Register.REG_RELAY0));
		registers.put("REG_RELAY1", new RegisterDescr(Register.REG_RELAY1));
		registers.put("REG_RELAY2", new RegisterDescr(Register.REG_RELAY2));
		registers.put("REG_RELAY3", new RegisterDescr(Register.REG_RELAY3));
		registers.put("REG_RELAY4", new RegisterDescr(Register.REG_RELAY4));
		registers.put("REG_RELAY5", new RegisterDescr(Register.REG_RELAY5));
		registers.put("REG_RELAY6", new RegisterDescr(Register.REG_RELAY6));
		registers.put("REG_RELAY7", new RegisterDescr(Register.REG_RELAY7));
		registers.put("REG_TEMPERATURE", new RegisterDescr(Register.REG_TEMP,
				true, false));
		registers.put("REG_TEMPERATURE2", new RegisterDescr(Register.REG_TEMP2,
				true, false));

		// registers.put("REG_EEPROM0", Register.REG_EEPROM0);
		// registers.put("REG_EEPROM1", Register.REG_EEPROM1);
		// registers.put("REG_EEPROM2", Register.REG_EEPROM2);
		// registers.put("REG_EEPROM3", Register.REG_EEPROM3);
		// registers.put("REG_EEPROM4", Register.REG_EEPROM4);
		// registers.put("REG_EEPROM5", Register.REG_EEPROM5);
		// registers.put("REG_EEPROM6", Register.REG_EEPROM6);
		// registers.put("REG_EEPROM7", Register.REG_EEPROM7);

		registers.put("REG_ADC0", new RegisterDescr(Register.REG_ADC0, true,
				false));
		registers.put("REG_ADC1", new RegisterDescr(Register.REG_ADC1, true,
				false));
		registers.put("REG_ADC2", new RegisterDescr(Register.REG_ADC2, true,
				false));
		registers.put("REG_ADC3", new RegisterDescr(Register.REG_ADC3, true,
				false));

		registers.put("REG_IN0", new RegisterDescr(Register.REG_IN0, true,
				false));
		registers.put("REG_IN1", new RegisterDescr(Register.REG_IN1, true,
				false));
		registers.put("REG_IN2", new RegisterDescr(Register.REG_IN2, true,
				false));
		registers.put("REG_IN3", new RegisterDescr(Register.REG_IN3, true,
				false));
		registers.put("REG_IN4", new RegisterDescr(Register.REG_IN4, true,
				false));
		registers.put("REG_IN5", new RegisterDescr(Register.REG_IN5, true,
				false));
		registers.put("REG_IN6", new RegisterDescr(Register.REG_IN6, true,
				false));
		registers.put("REG_IN7", new RegisterDescr(Register.REG_IN7, true,
				false));

		registers.put("REG_INPULLUP0",
				new RegisterDescr(Register.REG_INPULLUP0));
		registers.put("REG_INPULLUP1",
				new RegisterDescr(Register.REG_INPULLUP1));
		registers.put("REG_INPULLUP2",
				new RegisterDescr(Register.REG_INPULLUP2));
		registers.put("REG_INPULLUP3",
				new RegisterDescr(Register.REG_INPULLUP3));
		registers.put("REG_INPULLUP4",
				new RegisterDescr(Register.REG_INPULLUP4));
		registers.put("REG_INPULLUP5",
				new RegisterDescr(Register.REG_INPULLUP5));
		registers.put("REG_INPULLUP6",
				new RegisterDescr(Register.REG_INPULLUP6));
		registers.put("REG_INPULLUP7",
				new RegisterDescr(Register.REG_INPULLUP7));

	}

	protected List<RegisterUI> registerUIs = new ArrayList<RegisterUI>();

	protected void checkROReg(RegisterDescr descr) throws ParseException {
		if (descr.readonly)
			throw new ParseException("register is read only");
	}

	public LocalListDef locals = new LocalListDef();

	List<Byte> codeArr = new ArrayList<Byte>();

	public FuncDefList funcDefList = new FuncDefList();

	public Func getCreateFunc(String name, int args, boolean f)
			throws ParseException {
		Func func = funcDefList.get(name);
		if (func == null) {
			checkName(name);
			func = new Func(args, f);
			funcDefList.put(name, func);
		} else if (func.retSize != f) {
			throw new ParseException(name + " - ?");
		} else if (func.args != args)
			throw new ParseException(name + " argument number error");
		return func;
	}

	protected void newRegister(String regName, String regNum, boolean forceEE,
			Short initValue) throws ParseException {
		checkName(regName);
		RegisterDescr registerDescr;
		if (regNum == null) {
			if (forceEE)
				registerDescr = new RegisterDescr(nextEEReg++, true, true,
						initValue);
			else
				registerDescr = new RegisterDescr(nextReg++, false, false,
						initValue);
			if (nextEEReg > Register.REG_EEPROM0 + Register.REG_EEPROM_CNT
					|| nextReg > Register.REG_RAM0 + Register.REG_RAM_CNT)
				throw new ParseException("too many registers used");
		} else {
			registerDescr = registers.get(regNum);
			if (registerDescr == null)
				throw new ParseException(regNum + " - ?");
		}
		registers.put(regName, registerDescr);
	}

	protected void setUI(String regName, String uiName, RegType uiType)
			throws ParseException {
		RegisterDescr registerDescr = registers.get(regName);
		if (registerDescr == null)
			throw new ParseException(regName + " - ?");

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
			timer = new Timer(name, nextTimer++);
			timers.put(name, timer);
		}
		return timer;
	}

	protected Set<String> names = new HashSet<String>();

	protected void checkName(String name) throws ParseException {
		if (names.contains(name))
			throw new ParseException("name '" + name + "' already defined");
		names.add(name);
	}

	public void check() throws ParseException {
		for (Timer t : timers.values()) {
			if (t.handler == null)
				throw new ParseException("timer '" + t.name + "' not defined");
		}

		for (Func f : funcDefList.values()) {
			if (f.code == null)
				throw new ParseException((f.retSize ? "function" : "procedure")
						+ " '" + f.name + "' not defined");
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

		Func func = new Func(0, false);
		func.code = new CodeRef(this, code);
		funcDefList.setInit(func);
	}

	public void checkStack() throws ParseException {
		int maxStack = 0;

		for (Timer t : timers.values()) {
			int stack = t.getMaxStack();
			maxStack = Math.max(maxStack, stack);
		}

		for (Func f : funcDefList.values()) {
			int stack = f.getMaxStack();
			maxStack = Math.max(maxStack, stack);
		}

		for (Event e : events) {
			int stack = e.getCondMaxStack();
			maxStack = Math.max(maxStack, stack);
			stack = e.getHandlerMaxStack();
			maxStack = Math.max(maxStack, stack);
		}
		System.out.println("maxstack = " + maxStack);
	}

	public static byte[] dumpNull() throws IOException {
		return new byte[0];
	}

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

		for (byte b : codeArr)
			dos.write(b);

		dos.close();
		return baos.toByteArray();
	}

	public void run() {
		Collection<RTContext.Event> rtEvents = new ArrayList<RTContext.Event>();
		for (Event e : events)
			rtEvents.add(new RTContext.Event(e.cond, e.handler, e.type));

		RTContext.Timer rtTimers[] = new RTContext.Timer[timers.size()];
		for (Timer t : timers.values())
			rtTimers[t.n] = new RTContext.Timer(t.handler);

		RTContext.Func rtFuncs[] = new RTContext.Func[funcDefList.size()];
		for (Func f : funcDefList.values())
			rtFuncs[f.n] = new RTContext.Func(f.code);

		RTContext context = new RTContext(codeArr, rtTimers,
				rtEvents.toArray(new RTContext.Event[0]), rtFuncs);

		new VM(context);

	}

	public RegisterUI[] getRegisterDescriptions() {
		return registerUIs.toArray(new RegisterUI[0]);
	}

}
