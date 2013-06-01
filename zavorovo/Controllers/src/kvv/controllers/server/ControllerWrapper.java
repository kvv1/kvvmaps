package kvv.controllers.server;

import java.io.IOException;
import java.util.logging.Level;

import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.server.utils.MyLogger;

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
	public AllRegs getAllRegs(int addr) throws Exception {
		try {
			return controller.getAllRegs(addr);
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
	public void upload(int addr, byte[] data) throws IOException {
		try {
			controller.upload(addr, data);
		} catch (IOException e) {
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws Exception {
		try {
			return controller.getRegs(addr, reg, n);
		} catch (IOException e) {
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}
}
