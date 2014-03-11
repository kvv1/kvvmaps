package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kvv.controllers.register.RegType;
import kvv.controllers.register.Register;
import kvv.controllers.register.RegisterUI;
import kvv.evlang.ParseException;

public class Registers {
	protected Map<String, RegisterDescr> registers = new LinkedHashMap<String, RegisterDescr>();

	private short nextReg = Register.REG_RAM0;
	private short nextEEReg = Register.REG_EEPROM0;

	public List<Byte> refs = new ArrayList<Byte>();

	protected List<RegisterUI> registerUIs = new ArrayList<RegisterUI>();

	private final Context context;

	public Registers(Context context) {
		this.context = context;
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

	public void newRegisterAlias(String regName, String regNum)
			throws ParseException {
		context.checkName(regName);
		RegisterDescr registerDescr = registers.get(regNum);
		if (registerDescr == null)
			context.throwWatIsIt(regNum);
		registers.put(regName, registerDescr);
	}

	public RegisterDescr newVar(Type type, String regName, Expr initValue)
			throws ParseException {
		context.checkName(regName);
		if (nextReg >= Register.REG_RAM0 + Register.REG_RAM_CNT)
			context.throwExc("too many registers used");

		short n = nextReg++;
		RegisterDescr registerDescr = new RegisterDescr(type, n, false, false,
				null);
		registers.put(regName, registerDescr);

		if (type.isRef())
			refs.add((byte) n);

		if (initValue != null) {
			context.funcs.initFunc.code.addAll(initValue.getCode());
			context.funcs.initFunc.code.compileSetreg(registerDescr.reg);
		}

		return registerDescr;
	}

	public RegisterDescr newEERegister(String regName, Short initValue)
			throws ParseException {
		context.checkName(regName);
		RegisterDescr registerDescr = new RegisterDescr(Type.INT, nextEEReg++,
				true, true, initValue);

		if (nextEEReg > Register.REG_EEPROM0 + Register.REG_EEPROM_CNT)
			context.throwExc("too many registers used");
		registers.put(regName, registerDescr);

		context.funcs.initFunc.code.compileLit(initValue);
		context.funcs.initFunc.code.compileSetreg(registerDescr.reg);

		return registerDescr;
	}

	public RegisterDescr get(String regName) {
		return registers.get(regName);
	}

	public void setUI(String regName, String uiName, RegType uiType)
			throws ParseException {

		RegisterDescr registerDescr = registers.get(regName);
		if (registerDescr == null)
			context.throwWatIsIt(regName);

		if (uiType == RegType.textRW && !registerDescr.editable)
			uiType = RegType.textRO;

		registerUIs.add(new RegisterUI(registerDescr.reg, uiType, uiName));
	}

	public RegisterUI[] getUIRegisters() {
		return registerUIs.toArray(new RegisterUI[0]);
	}

}
