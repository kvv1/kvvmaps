package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.register.RegisterUI;
import kvv.controllers.server.Controllers;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;

public class ControllerWrapperUni extends ControllerAdapter {
	public ControllerWrapperUni(Controllers controllers, IController controller) {
		super(controllers, controller);
	}

	private ControllerType getType(int addr) throws IOException {
		try {
			ControllerDescr controllerDescr = controllers.get(addr);
			ControllerType controllerType = controllers.getControllerTypes()
					.get(controllerDescr.type);
			if (controllerType == null)
				throw new Exception("Тип контроллера " + controllerDescr.type
						+ " не определен");
			return controllerType;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		System.out.println("-" + addr + "(" + reg + ")=" + val);

		ControllerType controllerType = getType(addr);
		if (controllerType.def.relayRegsMul != null) {
			for (int i = 1; i < controllerType.def.relayRegsMul.length; i++)
				if (controllerType.def.relayRegsMul[i] == reg)
					val = val * controllerType.def.relayRegsMul[0];
		}

		wrapped.setReg(addr, reg, val);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		Integer i = adjustValue(addr, reg, wrapped.getReg(addr, reg));
		if (i == null)
			throw new IOException();
		return i;
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		int[] res = wrapped.getRegs(addr, reg, n);
		for (int i = 0; i < n; i++) {
			Integer ii = adjustValue(addr, reg + i, res[i]);
			if (ii == null)
				ii = -9999;
			res[i] = ii;
		}
		return res;
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		ControllerType controllerType = getType(addr);

		AllRegs allRegs;

		if (controllerType.def.regs != null) {
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			int[] vals = wrapped.getRegs(addr, controllerType.def.regs[0],
					controllerType.def.regs[1]);
			for (int i = 0; i < controllerType.def.regs[1]; i++)
				map.put(controllerType.def.regs[0] + i, vals[i]);
			allRegs = new AllRegs(addr, new ArrayList<RegisterUI>(), map);
		} else {
			allRegs = wrapped.getAllRegs(addr);
		}

		if (controllerType.def.relaysBitMapping != null) {
			Integer relays = allRegs.values
					.get(controllerType.def.relaysBitMapping[0]);
			for (int i = 1; i < controllerType.def.relaysBitMapping.length; i++)
				allRegs.values.put(controllerType.def.relaysBitMapping[i],
						(relays >> (i - 1)) & 1);
		}

		for (int reg : allRegs.values.keySet())
			allRegs.values.put(reg,
					adjustValue(addr, reg, allRegs.values.get(reg)));

		return allRegs;
	}

	private Integer adjustValue(int addr, int reg, Integer value)
			throws IOException {
		ControllerType controllerType = getType(addr);

		if (controllerType.def.relayRegsMul != null) {
			for (int i = 1; i < controllerType.def.relayRegsMul.length; i++)
				if (controllerType.def.relayRegsMul[i] == reg)
					value = value == 0 ? 0 : 1;
		}

		return value;
	}

}
