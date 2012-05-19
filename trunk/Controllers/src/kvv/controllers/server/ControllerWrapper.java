package kvv.controllers.server;

import java.io.IOException;
import java.util.Map;

import kvv.controllers.server.db.StateLog;
import kvv.controllers.server.rs485.IController;
import kvv.controllers.shared.Constants;

public class ControllerWrapper implements IController {

	private final IController controller;

	public ControllerWrapper(IController controller) {
		this.controller = controller;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		try {
			controller.setReg(addr, reg, val);
			StateLog.logState(Controllers.get(addr).name,
					Constants.REG_CONNECTION, 1);
			StateLog.logState(Controllers.get(addr).name, reg, val);
		} catch (IOException e) {
			StateLog.logState(Controllers.get(addr).name,
					Constants.REG_CONNECTION, 0);
			throw e;
		}
	}

	@Override
	public int getReg(int addr, int reg) throws Exception {
		int val;
		try {
			val = controller.getReg(addr, reg);
			StateLog.logState(Controllers.get(addr).name,
					Constants.REG_CONNECTION, 1);
			StateLog.logState(Controllers.get(addr).name, reg, val);
		} catch (IOException e) {
			StateLog.logState(Controllers.get(addr).name,
					Constants.REG_CONNECTION, 0);
			throw e;
		}
		return val;
	}

	@Override
	public Map<Integer, Integer> getRegs(int addr) throws Exception {
		Map<Integer, Integer> regs;
		try {
			regs = controller.getRegs(addr);
			StateLog.logState(Controllers.get(addr).name,
					Constants.REG_CONNECTION, 1);
			for (Integer reg : regs.keySet()) {
				if (reg < Constants.REG_ADC0 || reg >= Constants.REG_ADC0 + 8)
					StateLog.logState(Controllers.get(addr).name, reg,
							regs.get(reg));
			}
		} catch (IOException e) {
			StateLog.logState(Controllers.get(addr).name,
					Constants.REG_CONNECTION, 0);
			throw e;
		}
		return regs;
	}
}
