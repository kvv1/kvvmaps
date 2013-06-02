package kvv.evlang.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.controllers.register.Register;
import kvv.controllers.register.RegisterDescr;
import kvv.controllers.register.RegisterUI;
import kvv.evlang.ParseException;
import kvv.evlang.rt.RTContext;
import kvv.evlang.rt.VM;

public class Context {
	protected Map<String, Integer> constants = new HashMap<String, Integer>();

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

		// registers.put("REG_EEPROM0", Register.REG_EEPROM0);
		// registers.put("REG_EEPROM1", Register.REG_EEPROM1);
		// registers.put("REG_EEPROM2", Register.REG_EEPROM2);
		// registers.put("REG_EEPROM3", Register.REG_EEPROM3);
		// registers.put("REG_EEPROM4", Register.REG_EEPROM4);
		// registers.put("REG_EEPROM5", Register.REG_EEPROM5);
		// registers.put("REG_EEPROM6", Register.REG_EEPROM6);
		// registers.put("REG_EEPROM7", Register.REG_EEPROM7);

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

	public static class ArgListDef {
		private Map<String, Integer> args = new LinkedHashMap<String, Integer>();
		private int argCnt;

		public void add(String name) {
			args.put(name, args.size());
		}

		public void endOfArgs() {
			argCnt = args.size();
		}

		public int getArgCnt() {
			return argCnt;
		}

		public void clear() {
			args.clear();
			argCnt = 0;
		}

		public Integer get(String name) {
			return args.get(name);
		}
	}

	public ArgListDef args = new ArgListDef();

	List<Byte> codeArr = new ArrayList<Byte>();

	public class CodeRef {
		public int off;
		public int len;

		public CodeRef() {
		}

		public CodeRef(Code code) {
			this.off = codeArr.size();
			this.len = code.size();
			codeArr.addAll(code.code);
		}

		public int check(int expected, String msg) throws ParseException {
			int maxStack = 0;
			int stack = 0;
			for (int i = off; i < off + len; i++) {
				int bc1 = codeArr.get(i) & 0xC0;
				if (bc1 == BC.GETREGSHORT) {
					EG.dumpStream.print("GETREGSHORT ");
					stack++;
				} else if (bc1 == BC.SETREGSHORT) {
					EG.dumpStream.print("SETREGSHORT ");
					stack--;
				} else if (bc1 == BC.LITSHORT) {
					EG.dumpStream.print("LITSHORT ");
					stack += 1;
				} else {
					bc1 = codeArr.get(i);

					BC bc = BC.values()[bc1];

					EG.dumpStream.print(bc.name() + " ");

					if (bc == BC.CALLF || bc == BC.CALLP) {
						PrintStream ps = EG.dumpStream;
						EG.dumpStream = EG.nullStream;
						Func f = funcValues.get(codeArr.get(i + 1));
						maxStack = Math.max(maxStack,
								stack + 2 + f.getMaxStack());
						stack -= f.args;
						if (f.f)
							stack++;
						i += bc.args;
						EG.dumpStream = ps;
					} else if (bc == BC.RET || bc == BC.RETI || bc == BC.RET_N
							|| bc == BC.RETI_N) {
						break;
					} else {
						stack += bc.stackBalance;
						i += bc.args;
					}
				}
				if (stack < 0)
					throw new ParseException(msg + " stack underflow");

				maxStack = Math.max(maxStack, stack);
			}
			if (stack != expected)
				throw new ParseException(msg + " stack error");

			return maxStack;
		}
	}

	public class Func {
		public int n;
		public CodeRef code;
		public boolean f;
		public String name;
		public int args;

		public int maxStack = -1;

		public Func(String name, int args, int n, boolean f) {
			this.name = name;
			this.args = args;
			this.n = n;
			this.f = f;
		}

		int getMaxStack() throws ParseException {
			String msg = (f ? "function" : "procedure") + " '" + name + "'";
			if (maxStack < 0) {
				EG.dumpStream.print(msg + " ");
				maxStack = code.check(f ? 1 : 0, msg);
				EG.dumpStream.println("maxStack: " + maxStack);
			}
			return maxStack;
		}
	}

	protected List<Func> funcValues = new ArrayList<Func>();
	{
		funcValues.add(null);
	}

	protected Map<String, Func> funcs = new LinkedHashMap<String, Func>();

	public Func getCreateFunc(String name, int args, boolean f)
			throws ParseException {
		Func func = funcs.get(name);
		if (func == null) {
			checkName(name);
			func = new Func(name, args, funcValues.size(), f);
			funcs.put(name, func);
			funcValues.add(func);
		} else if (func.f != f) {
			throw new ParseException(name + " - ?");
		} else if (func.args != args)
			throw new ParseException(name + " argument number error");
		return func;
	}

	public enum EventType {
		SET, CHANGE
	}

	public class Event {
		public CodeRef cond;
		public CodeRef handler;

		public EventType type;

		public Event(Code cond, Code handler, EventType type) {
			this.cond = new CodeRef(cond);
			this.handler = new CodeRef(handler);
			this.type = type;
		}

		public int getCondMaxStack() throws ParseException {
			String msg = "event cond";
			EG.dumpStream.print(msg + " ");
			int maxStack = cond.check(1, msg);
			EG.dumpStream.println("maxStack: " + maxStack);
			return maxStack;
		}

		public int getHandlerMaxStack() throws ParseException {
			String msg = "event handler";
			EG.dumpStream.print(msg + " ");
			int maxStack = handler.check(0, msg);
			EG.dumpStream.println("maxStack: " + maxStack);
			return maxStack;
		}
	}

	public Collection<Event> events = new ArrayList<Event>();

	public class Timer {
		public int n;
		public CodeRef handler;
		public String name;

		public Timer(String name, int n) {
			this.name = name;
			this.n = n;
		}

		public int getMaxStack() throws ParseException {
			String msg = "timer '" + name + "'";
			EG.dumpStream.print(msg + " ");
			int maxStack = handler.check(0, msg);
			EG.dumpStream.println("maxStack: " + maxStack);
			return maxStack;
		}
	}

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

		for (Func f : funcValues) {
			if (f.code == null)
				throw new ParseException((f.f ? "function" : "procedure")
						+ " '" + f.name + "' not defined");
		}

	}

	public void checkStack() throws ParseException {
		int maxStack = 0;

		for (Timer t : timers.values()) {
			int stack = t.getMaxStack();
			maxStack = Math.max(maxStack, stack);
		}

		for (Func f : funcValues) {
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

		dos.writeByte(funcValues.size());
		for (Func f : funcValues) {
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

		RTContext.Func rtFuncs[] = new RTContext.Func[funcValues.size()];
		for (Func f : funcValues)
			rtFuncs[f.n] = new RTContext.Func(f.code);

		RTContext context = new RTContext(codeArr, rtTimers,
				rtEvents.toArray(new RTContext.Event[0]), rtFuncs);

		new VM(context);

	}

	public RegisterUI[] getRegisterDescriptions() {
		return registerUIs.toArray(new RegisterUI[0]);
	}

}
