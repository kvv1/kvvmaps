package kvv.controllers.server.controller;

import java.io.IOException;

import kvv.controllers.controller.IController;
import kvv.controllers.shared.ControllerDef.RegisterDef;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.SystemDescr;

public class ControllerWrapperAdj extends ControllerAdapter {

	public ControllerWrapperAdj(SystemDescr system, IController wrapped) {
		super(system, wrapped);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		
		RegisterDef registerDef = getRegDef(addr, reg);

		if (registerDef != null && registerDef.mul != null)
			val *= registerDef.mul;
		
		wrapped.setReg(addr, reg, val);
	}

	@Override
	public Integer getReg(int addr, int reg) throws IOException {
		Integer res = adjust(addr, reg, wrapped.getReg(addr, reg));
		return res;
	}

	@Override
	public Integer[] getRegs(int addr, int reg, int n) throws IOException {
		Integer[] res = wrapped.getRegs(addr, reg, n);
		for (int i = 0; i < n; i++) {
			Integer v = adjust(addr, reg + i, res[i]);
			res[i] = v;
		}
		return res;
	}

	private Integer adjust(int addr, int reg, Integer value) {
		if (value == null)
			return null;
		RegisterDef registerDef = getRegDef(addr, reg);
		if (registerDef != null) {
//			if(registerDef.mul != null)
//				value /= registerDef.mul;
			if (registerDef.validRanges != null) {
				boolean ok = false;
				for (int i = 0; i < registerDef.validRanges.length; i += 2)
					if (value >= registerDef.validRanges[i]
							&& value < registerDef.validRanges[i + 1])
						ok = true;
				if (!ok)
					return null;
			}
		}
		return value;
	}

	private RegisterDef getRegDef(int addr, int reg) {
		ControllerDescr cd = system.getController(addr);
		if (cd == null)
			return null;
		
		ControllerType ct = system.controllerTypes.get(cd.type);
		if (ct == null)
			return null;
		
		return ct.def.getReg(reg);
	}

}
