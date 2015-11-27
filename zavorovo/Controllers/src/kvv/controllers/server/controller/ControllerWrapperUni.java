package kvv.controllers.server.controller;

import java.io.IOException;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.shared.ControllerDef.RegisterDef;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;

public class ControllerWrapperUni extends ControllerAdapter {
	public ControllerWrapperUni(Controllers controllers, IController controller) {
		super(controllers, controller);
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

	private Integer adjustValue(int addr, int reg, Integer value)
			throws IOException {
		ControllerDescr controllerDescr = controllers.get(addr);
		ControllerType controllerType = controllers.getControllerTypes().get(
				controllerDescr.type);
		if (controllerType == null)
			throw new ControllerTypeNotFoundException(controllerDescr.type);
		RegisterDef registerDef = controllerType.def.getReg(reg);
		if (registerDef == null)
			throw new RegisterNotFoundException(addr, reg);

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
