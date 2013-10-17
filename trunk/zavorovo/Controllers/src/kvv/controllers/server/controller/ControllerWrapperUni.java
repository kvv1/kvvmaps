package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.register.Register;
import kvv.controllers.register.RegisterUI;
import kvv.controllers.server.Controllers;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerDescr.Type;

class ControllerWrapperUni implements IController {
	private final IController controller;

	public ControllerWrapperUni(IController controller) {
		this.controller = controller;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		try {
			if (Controllers.getInstance().get(addr).type == Type.MU110_8)
				val = val == 0 ? 0 : 1000;
		} catch (Exception e) {
		}
		controller.setReg(addr, reg, val);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		Integer i = adjustValue(addr, reg, controller.getReg(addr, reg));
		if (i == null)
			throw new IOException();
		return i;
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		int[] res = controller.getRegs(addr, reg, n);
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
		ControllerDescr controllerDescr;
		try {
			controllerDescr = Controllers.getInstance().get(addr);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		AllRegs allRegs;

		switch (controllerDescr.type) {
		case MU110_8:
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			int[] vals = controller.getRegs(addr, 0, 8);
			for (int i = 0; i < 8; i++)
				map.put(i, vals[i]);
			allRegs = new AllRegs(addr, new ArrayList<RegisterUI>(), map);
			break;
		case TYPE2:
			allRegs = controller.getAllRegs(addr);
			int relays = allRegs.values.get(Register.REG_RELAYS);
			for (int i = 0; i < Register.REG_RELAY_CNT; i++)
				allRegs.values.put(Register.REG_RELAY0 + i, (relays >> i) & 1);
			break;

		default:
			throw new IOException("unknown controller type");
		}

		for (int reg : allRegs.values.keySet())
			allRegs.values.put(reg,
					adjustValue(addr, reg, allRegs.values.get(reg)));

		return allRegs;
	}

	@Override
	public void upload(int addr, byte[] data) throws IOException {
		controller.upload(addr, data);
	}

	@Override
	public void close() {
		controller.close();
	}

	@Override
	public void vmInit(int addr) throws IOException {
		controller.vmInit(addr);
	}

	private static Integer adjustValue(int addr, int reg, Integer value)
			throws IOException {
		ControllerDescr controllerDescr;
		try {
			controllerDescr = Controllers.getInstance().get(addr);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		switch (controllerDescr.type) {
		case MU110_8:
			value = value == 0 ? 0 : 1;
			break;
		case TYPE2:
			if ((reg == Register.REG_TEMP || reg == Register.REG_TEMP2)
					&& value != null && value == -9999)
				value = null;
			break;
		}
		return value;
	}

}
