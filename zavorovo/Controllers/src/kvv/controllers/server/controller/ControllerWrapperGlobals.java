package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.HashMap;

import kvv.controller.register.AllRegs;
import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;

public class ControllerWrapperGlobals extends ControllerAdapter {

	public ControllerWrapperGlobals(Controllers controllers, IController wrapped) {
		super(controllers, wrapped);
	}

	private final AllRegs globals = new AllRegs(0,
			new HashMap<Integer, Integer>());
	{
		for (int i = 0; i < 256; i++)
			globals.values.put(i, 0);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		if (addr != 0) {
			wrapped.setReg(addr, reg, val);
		} else {
			globals.values.put(reg, val);
		}
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		if (addr != 0) {
			return wrapped.getReg(addr, reg);
		} else {
			return globals.values.get(reg);
		}
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		if (addr != 0) {
			return wrapped.getRegs(addr, reg, n);
		} else {
			int[] res = new int[n];
			for (int i = 0; i < n; i++)
				res[i] = globals.values.get(reg + i);
			return res;
		}
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		if (addr != 0) {
			return wrapped.getAllRegs(addr);
		} else {
			AllRegs ar = new AllRegs(0, new HashMap<Integer, Integer>());
			for (int r : globals.values.keySet())
				ar.values.put(r, globals.values.get(r));
			return ar;
		}
	}
}
