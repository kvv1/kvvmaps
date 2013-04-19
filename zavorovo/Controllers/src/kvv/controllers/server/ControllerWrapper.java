package kvv.controllers.server;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import kvv.controllers.controller.IController;
import kvv.controllers.utils.MyLogger;

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
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}

	@Override
	public int getReg(int addr, int reg) throws Exception {
		try {
			return controller.getReg(addr, reg);
		} catch (IOException e) {
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}

	@Override
	public Map<Integer, Integer> getRegs(int addr) throws Exception {
		try {
			return controller.getRegs(addr);
		} catch (IOException e) {
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}

	@Override
	public void close() {
		controller.close();
	}

	@Override
	public void upload(int addr, int start, byte[] data) throws IOException {
		try {
			controller.upload(addr, start, data);
		} catch (IOException e) {
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}

	@Override
	public void upload(int addr, byte[] data) throws IOException {
		try {
			controller.upload(addr, data);
		} catch (IOException e) {
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}
}
