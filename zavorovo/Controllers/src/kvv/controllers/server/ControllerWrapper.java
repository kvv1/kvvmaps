package kvv.controllers.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.register.Register;
import kvv.controllers.server.history.HistoryLogger;
import kvv.controllers.server.utils.MyLogger;

public class ControllerWrapper implements IController {

	private final IController controller;

	public ControllerWrapper(IController controller) {
		this.controller = controller;
	}

	@Override
	public void close() {
		controller.close();
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		try {
			controller.setReg(addr, reg, val);
			HistoryLogger.log(addr, reg, val);
		} catch (IOException e) {
			HistoryLogger.log(addr, null);
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
			AllRegs allRegs = controller.getAllRegs(addr);
			adjust(allRegs);
			HistoryLogger.log(addr, allRegs.values);
			return allRegs;
		} catch (IOException e) {
			HistoryLogger.log(addr, null);
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}

	private void adjust(AllRegs allRegs) {
		int relays = allRegs.values.get(Register.REG_RELAYS);
		for (int i = 0; i < Register.REG_RELAY_CNT; i++)
			allRegs.values.put(Register.REG_RELAY0 + i, (relays >> i) & 1);

		Integer t = allRegs.values.get(Register.REG_TEMP);
		if (t != null && t == -9999)
			allRegs.values.put(Register.REG_TEMP, null);
		t = allRegs.values.get(Register.REG_TEMP2);
		if (t != null && t == -9999)
			allRegs.values.put(Register.REG_TEMP2, null);
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws Exception {
		try {
			int[] res = controller.getRegs(addr, reg, n);
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (int i = 0; i < n; i++)
				map.put(reg + i, res[i]);
			HistoryLogger.log(addr, map);
			return res;
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

	@Override
	public void vmInit(int addr) throws IOException {
		try {
			controller.vmInit(addr);
		} catch (IOException e) {
			MyLogger.getLogger().log(Level.WARNING, e.getMessage());
			throw e;
		}
	}
}
