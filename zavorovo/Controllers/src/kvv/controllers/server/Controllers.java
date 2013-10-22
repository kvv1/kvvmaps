package kvv.controllers.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.Register;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

public class Controllers {

	private static Controllers instance;

	public static synchronized Controllers getInstance() {
		if (instance == null)
			instance = new Controllers();
		return instance;
	}

	public static synchronized void reload() {
		instance = null;
	}

	private final Map<String, ControllerDescr> nameMap = new HashMap<String, ControllerDescr>();
	private final Map<Integer, ControllerDescr> addrMap = new HashMap<Integer, ControllerDescr>();

	private final List<ControllerDescr> controllers = new ArrayList<ControllerDescr>();
	private final Map<String, Register> registers = new HashMap<String, Register>();
	private final Map<Integer, Register> ar2register = new HashMap<Integer, Register>();

	{
		int nextGlobalReg = 0;

		try {
			ControllerDescr[] controllers1 = Utils.jsonRead(
					Constants.controllersFile, ControllerDescr[].class);

			for (ControllerDescr c : controllers1) {
				if (c != null) {
					Register[] regs = c.registers;
					if (regs != null) {
						for (Register reg : regs) {
							if (reg != null) {
								if (c.name == null) {
									reg.register = nextGlobalReg++;
								} else {
									reg.controller = c.name;
									reg.addr = c.addr;
								}
								if (reg.scaleLevels == null)
									reg.scaleLevels = new int[] { 0, 1 };
								registers.put(reg.name, reg);
								ar2register.put(
										(reg.addr << 16) + reg.register, reg);
							}
						}
						// c.registers = null;
					}
					if (c.name == null)
						c.name = "";
					controllers.add(c);
					nameMap.put(c.name, c);
					addrMap.put(c.addr, c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ControllerDescr get(String name) throws Exception {
		ControllerDescr d = nameMap.get(name);
		if (d == null)
			throw new Exception("Контроллер с именем " + name + " не определен");
		return d;
	}

	public ControllerDescr get(int addr) throws Exception {
		ControllerDescr d = addrMap.get(addr);
		if (d == null)
			throw new Exception("Контроллер с адресом " + addr
					+ " не определен");
		return d;
	}

	public Map<String, Register> getRegisters() {
		return registers;
	}

	public Register getRegister(String name) throws Exception {
		Register reg = registers.get(name);
		if (reg == null)
			throw new Exception("Регистр " + name + " не определен");
		return reg;
	}

	public Register getRegister(int addr, int reg) {
		return ar2register.get((addr << 16) + reg);
	}

	public Collection<Register> getRegisters(int addr) {
		Collection<Register> regs = new ArrayList<Register>();
		for (Register reg : registers.values())
			if (reg.addr == addr)
				regs.add(reg);
		return regs;
	}

	public ControllerDescr[] getControllers() {
		return controllers.toArray(new ControllerDescr[0]);
	}

	public static void save(String text) throws IOException {
		Utils.writeFile(Constants.controllersFile, text);
		reload();
	}

	public static String load() throws IOException {
		return Utils.readFile(Constants.controllersFile);
	}

}
