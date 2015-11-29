package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.HashMap;

import kvv.controllers.controller.IController;
import kvv.controllers.shared.SystemDescr;

public class ControllerWrapperGlobals extends ControllerAdapter {

	public ControllerWrapperGlobals(SystemDescr system, IController wrapped) {
		super(system, wrapped);
	}

	private final HashMap<Integer, Integer> globals = new HashMap<>();

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		if (addr != 0)
			wrapped.setReg(addr, reg, val);
		else
			globals.put(reg, val);
	}

	@Override
	public Integer getReg(int addr, int reg) throws IOException {
		if (addr != 0)
			return wrapped.getReg(addr, reg);
		else
			return globals.get(reg);
	}

	@Override
	public Integer[] getRegs(int addr, int reg, int n) throws IOException {
		if (addr != 0) {
			return wrapped.getRegs(addr, reg, n);
		} else {
			Integer[] res = new Integer[n];
			for (int i = 0; i < n; i++)
				res[i] = globals.get(reg);
			return res;
		}
	}
}
