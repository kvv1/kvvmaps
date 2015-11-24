package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.HashMap;

import kvv.controller.register.AllRegs;
import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.ControllerDef.RegisterDef;

public class ControllerWrapperUni extends ControllerAdapter {
	public ControllerWrapperUni(Controllers controllers, IController controller) {
		super(controllers, controller);
	}

	private ControllerType getType(int addr) throws IOException {
		ControllerDescr controllerDescr = controllers.get(addr);
		ControllerType controllerType = controllers.getControllerTypes().get(
				controllerDescr.type);
		if (controllerType == null)
			throw new ControllerTypeNotFoundException(controllerDescr.type);
		return controllerType;
	}

	private RegisterDef getRegDef(int addr, int reg) throws IOException {
		ControllerType controllerType = getType(addr);
		RegisterDef registerDef = controllerType.def.getReg(reg);
		if (registerDef == null)
			throw new RegisterNotFoundException(addr, reg);
		return registerDef;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		wrapped.setReg(addr, reg, val);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		int val = wrapped.getReg(addr, reg);
		Integer val1 = adjustValue(addr, reg, val);
		if (val1 == null)
			throw new InvalidRegisterValueException(addr, reg, val);
		return val1;
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		int[] res = wrapped.getRegs(addr, reg, n);
		for (int i = 0; i < n; i++) {
			try {
				Integer ii = adjustValue(addr, reg + i, res[i]);
				if (ii == null)
					ii = -9999;
				res[i] = ii;
			} catch (IOException e) {
				res[i] = 0;
			}
		}
		return res;
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		ControllerType controllerType = getType(addr);

		AllRegs allRegs;

		if (controllerType.def.allRegs != null) {
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			int[] vals = wrapped.getRegs(addr, controllerType.def.allRegs[0],
					controllerType.def.allRegs[1]);
			for (int i = 0; i < controllerType.def.allRegs[1]; i++)
				map.put(controllerType.def.allRegs[0] + i, vals[i]);
			allRegs = new AllRegs(addr, map);
		} else {
			allRegs = wrapped.getAllRegs(addr);
		}

		HashMap<Integer, Integer> values1 = new HashMap<Integer, Integer>();
/*
		for (int reg : allRegs.values.keySet()) {
			try {
				RegisterDef registerDef = getRegDef(addr, reg);
				if (registerDef.bitMapping != null) {
					int relays = allRegs.values.get(reg);
					for (int i = 0; i < registerDef.bitMapping.length; i++)
						values1.put(registerDef.bitMapping[i],
								(relays >> i) & 1);
				}
			} catch (IOException e) {
			}
		}
*/
		allRegs.values.putAll(values1);

		// for (int key : allRegs.values.keySet())
		// System.out.print(key + ":" + allRegs.values.get(key) + " ");
		// System.out.println();

		for (int reg : allRegs.values.keySet()) {
			try {
				allRegs.values.put(reg,
						adjustValue(addr, reg, allRegs.values.get(reg)));
			} catch (IOException e) {
				allRegs.values.put(reg, null);
			}
		}

		return allRegs;
	}

	private Integer adjustValue(int addr, int reg, Integer value)
			throws IOException {
		RegisterDef registerDef = getRegDef(addr, reg);

		if (registerDef.validRanges != null) {
			boolean ok = false;
			for (int i = 0; i < registerDef.validRanges.length; i += 2)
				if (value >= registerDef.validRanges[i]
						&& value < registerDef.validRanges[i + 1])
					ok = true;
			if (!ok)
				return null;
		}

		return value;
	}

}
