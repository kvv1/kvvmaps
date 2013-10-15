package kvv.controllers.server;

import java.io.IOException;
import java.util.HashMap;

import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.register.Register;
import kvv.controllers.server.history.HistoryLogger;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerDescr.Type;

public class ControllerWrapper implements IController {

	private final IController controller;

	public ControllerWrapper(IController controller) {
		this.controller = controller;
	}

	@Override
	public void close() {
		controller.close();
	}

	private static int adjustValue(int addr, int value) {
		ControllerDescr controllerDescr;
		try {
			controllerDescr = Controllers.getInstance().get(addr);
			if (controllerDescr.type == Type.MU110_8)
				value = value == 0 ? 0 : 1;
		} catch (Exception e) {
		}
		return value;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		try {
			int val1 = val;
			try {
				if (Controllers.getInstance().get(addr).type == Type.MU110_8)
					val = val == 0 ? 0 : 1000;
			} catch (Exception e) {
			}
			controller.setReg(addr, reg, val);
			HistoryLogger.log(addr, reg, val1);
		} catch (IOException e) {
			HistoryLogger.log(addr, reg, null);
			throw e;
		}
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		try {
			int val = adjustValue(addr, controller.getReg(addr, reg));
			HistoryLogger.log(addr, reg, val);
			return val;
		} catch (IOException e) {
			HistoryLogger.log(addr, reg, null);
			throw e;
		}
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		try {
			AllRegs allRegs = controller.getAllRegs(addr);
			adjust(allRegs);
			HistoryLogger.log(addr, allRegs.values);
			return allRegs;
		} catch (IOException e) {
			HistoryLogger.log(addr, null);
			throw e;
		}
	}

	private static void adjust(AllRegs allRegs) {
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
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		try {
			int[] res = controller.getRegs(addr, reg, n);
			for (int i = 0; i < n; i++)
				res[i] = adjustValue(addr, res[i]);
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (int i = 0; i < n; i++)
				map.put(reg + i, res[i]);
			HistoryLogger.log(addr, map);
			return res;
		} catch (IOException e) {
			HistoryLogger.log(addr, null);
			throw e;
		}
	}

	@Override
	public void upload(int addr, byte[] data) throws IOException {
		controller.upload(addr, data);
	}

	@Override
	public void vmInit(int addr) throws IOException {
		controller.vmInit(addr);
	}
}
