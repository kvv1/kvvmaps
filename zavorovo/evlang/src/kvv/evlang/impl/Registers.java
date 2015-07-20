package kvv.evlang.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kvv.controller.register.ControllerDef;
import kvv.controller.register.RegType;
import kvv.controller.register.RegisterUI;
import kvv.controllers.utils.Constants;
import kvv.evlang.ParseException;
import kvv.stdutils.Utils;

public class Registers {
	protected Map<String, RegisterDescr> registers = new LinkedHashMap<String, RegisterDescr>();

	public ControllerDef controllerDef;
	private int nextReg;
	private int nextEEReg;
	private int lastReg;
	private int lastEEReg;

	public List<Byte> refs = new ArrayList<Byte>();

	protected List<RegisterUI> registerUIs = new ArrayList<RegisterUI>();

	private final Context context;

	public Registers(Context context, String controllerType) throws IOException {
		this.context = context;

		if (controllerType != null) {

			controllerDef = Utils.jsonRead(Constants.controllerTypesDir
					+ controllerType + "/def.json", ControllerDef.class);

			nextReg = controllerDef.regRAM0;
			nextEEReg = controllerDef.regEEPROM0;
			lastReg = controllerDef.lastReg;
			lastEEReg = controllerDef.lastEEReg;

			Properties prop = new Properties();
			prop.load(new FileInputStream(Constants.controllerTypesDir
					+ controllerType + "/reg.properties"));

			for (Object key : prop.keySet())
				registers.put(
						(String) key,
						new RegisterDescr(Type.INT, Integer.parseInt(prop
								.getProperty((String) key))));
		} else {
			nextReg = 0;
			lastReg = 100;
		}
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
		if (nextReg >= lastReg)
			context.throwExc("too many registers used");

		int n = nextReg++;
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
		if (nextEEReg >= lastEEReg)
			context.throwExc("too many registers used");

		RegisterDescr registerDescr = new RegisterDescr(Type.INT, nextEEReg++,
				true, true, initValue);

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
