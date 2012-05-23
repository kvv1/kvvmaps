package kvv.controllers.server;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import kvv.controllers.server.rs485.IController;

public class ControllerWrapper implements IController {

	private final IController controller;

	public ControllerWrapper(IController controller) {
		this.controller = controller;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		try {
			controller.setReg(addr, reg, val);
		} catch (IOException e) {
			Utils.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}

	@Override
	public int getReg(int addr, int reg) throws Exception {
		int val;
		try {
			val = controller.getReg(addr, reg);
		} catch (IOException e) {
			Utils.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
		return val;
	}

	@Override
	public Map<Integer, Integer> getRegs(int addr) throws Exception {
		Map<Integer, Integer> regs;
		try {
			regs = controller.getRegs(addr);
		} catch (IOException e) {
			Utils.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
		return regs;
	}
}
