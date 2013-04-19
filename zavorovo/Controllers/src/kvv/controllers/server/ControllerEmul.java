package kvv.controllers.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import kvv.controllers.controller.IController;
import kvv.controllers.register.Register;
import kvv.controllers.server.utils.MyLogger;

public class ControllerEmul implements IController {

	private HashMap<Integer, HashMap<Integer, Integer>> map = new HashMap<Integer, HashMap<Integer, Integer>>();

	private final static int delay = 200;

	@Override
	public synchronized void setReg(int addr, int reg, int val)
			throws IOException {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		HashMap<Integer, Integer> regs = map.get(addr);
		if (regs == null) {
			regs = new HashMap<Integer, Integer>();
			for (int i = 0; i < Register.REG_ADC0 + 8; i++)
				regs.put(i, 0);
			map.put(addr, regs);
		}

		regs.put(reg, val);
		if (reg == Register.REG_RELAYS) {
			for (int i = 0; i < 8; i++) {
				regs.put(i, (val & 1));
				val >>= 1;
			}
		} else if (reg < 8) {
			if (val != 0)
				regs.put(Register.REG_RELAYS, regs.get(Register.REG_RELAYS)
						| (1 << reg));
			else
				regs.put(Register.REG_RELAYS, regs.get(Register.REG_RELAYS)
						& ~(1 << reg));
		}
	}

	@Override
	public synchronized int getReg(int addr, int reg) throws IOException {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return _getReg(addr, reg);
	}

	private int _getReg(int addr, int reg) {
		HashMap<Integer, Integer> regs = map.get(addr);
		if (regs == null)
			return 0;
		Integer val = regs.get(reg);
		return val == null ? 0 : val.intValue();
	}

	@Override
	public synchronized Map<Integer, Integer> getRegs(int addr)
			throws IOException {
		MyLogger.getLogger().log(Level.WARNING, "xaxaxa");

		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (addr == 3) {
			throw new IOException();
		}

		HashMap<Integer, Integer> regs = map.get(addr);
		if (regs == null) {
			regs = new HashMap<Integer, Integer>();
			for (int i = 0; i < Register.REG_ADC0 + 8; i++)
				regs.put(i, 0);
		}
		return regs;
	}

	@Override
	public void close() {
	}

	@Override
	public void upload(int addr, int start, byte[] data) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void upload(int addr, byte[] data) throws IOException {
		throw new UnsupportedOperationException();
	}
}
