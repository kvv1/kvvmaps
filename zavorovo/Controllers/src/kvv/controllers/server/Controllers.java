package kvv.controllers.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.Register;
import kvv.controllers.utils.Constants;

public class Controllers {
	private final static Map<String, ControllerDescr> nameMap = new HashMap<String, ControllerDescr>();
	private final static Map<Integer, ControllerDescr> addrMap = new HashMap<Integer, ControllerDescr>();

	private final static List<ControllerDescr> controllers = new ArrayList<ControllerDescr>();
	private static Map<String, Register> registers = new HashMap<String, Register>();
	private static Map<Integer, Register> ar2register = new HashMap<Integer, Register>();

	static {
		try {
			ControllerDescr[] controllers1 = Utils.jsonRead(
					Constants.controllersFile, ControllerDescr[].class);

			for (ControllerDescr c : controllers1) {
				if (c != null) {
					Register[] regs = c.registers;
					if (regs != null) {
						for (Register reg : regs) {
							if (reg != null) {
								reg.controller = c.name;
								reg.addr = c.addr;
								registers.put(reg.name, reg);
								ar2register.put(
										(reg.addr << 16) + reg.register, reg);
							}
						}
						c.registers = null;
					}
					controllers.add(c);
					nameMap.put(c.name, c);
					addrMap.put(c.addr, c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ControllerDescr get(String name) throws Exception {
		ControllerDescr d = nameMap.get(name);
		if (d == null)
			throw new Exception("Контроллер с именем " + name + " не определен");
		return d;
	}

	public static ControllerDescr get(int addr) throws Exception {
		ControllerDescr d = addrMap.get(addr);
		if (d == null)
			throw new Exception("Контроллер с адресом " + addr
					+ " не определен");
		return d;
	}

	public synchronized static Map<String, Register> getRegisters() {
		return registers;
	}

	public synchronized static Register getRegister(String name)
			throws Exception {
		Register reg = registers.get(name);
		if (reg == null)
			throw new Exception("Регистр " + name + " не определен");
		return reg;
	}

	public synchronized static Register getRegister(int addr, int reg) {
		return ar2register.get((addr << 16) + reg);
	}

	public static Collection<Register> getRegisters(int addr) {
		Collection<Register> regs = new ArrayList<Register>();
		for (Register reg : registers.values())
			if (reg.addr == addr)
				regs.add(reg);
		return regs;
	}

	public static ControllerDescr[] getControllers() {
		return controllers.toArray(new ControllerDescr[0]);
	}

}
