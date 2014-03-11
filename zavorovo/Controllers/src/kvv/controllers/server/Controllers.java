package kvv.controllers.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kvv.controllers.server.context.Context;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

public class Controllers {
	private final Map<String, ControllerDescr> nameMap = new HashMap<String, ControllerDescr>();
	private final Map<Integer, ControllerDescr> addrMap = new HashMap<Integer, ControllerDescr>();

	private final List<ControllerDescr> controllers = new ArrayList<ControllerDescr>();
	private final Map<String, RegisterDescr> registers = new HashMap<String, RegisterDescr>();
	private final Map<Integer, RegisterDescr> ar2register = new HashMap<Integer, RegisterDescr>();

	{
		int nextGlobalReg = 0;

		try {
			ControllerDescr[] controllers1 = Utils.jsonRead(
					Constants.controllersFile, ControllerDescr[].class);

			for (ControllerDescr c : controllers1) {
				if (c != null) {
					RegisterDescr[] regs = c.registers;
					if (regs != null) {
						for (RegisterDescr reg : regs) {
							if (reg != null) {
								if (c.name == null) {
									reg.register = nextGlobalReg++;
									reg.addr = 0;
								} else {
									reg.controller = c.name;
									reg.addr = c.addr;
								}
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

	public Map<String, RegisterDescr> getRegisters() {
		return registers;
	}

	public RegisterDescr getRegister(String name) throws Exception {
		RegisterDescr reg = registers.get(name);
		if (reg == null)
			throw new Exception("Регистр " + name + " не определен");
		return reg;
	}

	public RegisterDescr getRegister(int addr, int reg) {
		return ar2register.get((addr << 16) + reg);
	}

	public Collection<RegisterDescr> getRegisters(int addr) {
		Collection<RegisterDescr> regs = new ArrayList<RegisterDescr>();
		for (RegisterDescr reg : registers.values())
			if (reg.addr == addr)
				regs.add(reg);
		return regs;
	}

	public ControllerDescr[] getControllers() {
		return controllers.toArray(new ControllerDescr[0]);
	}

	public static void save(ControllerDescr[] controllers) throws Exception {
		Utils.jsonWrite(Constants.controllersFile, controllers);
		Context.reload();
	}

}
