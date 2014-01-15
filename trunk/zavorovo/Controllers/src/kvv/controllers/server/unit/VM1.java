package kvv.controllers.server.unit;

import java.io.IOException;

import kvv.controllers.controller.IController;
import kvv.evlang.rt.Const;
import kvv.evlang.rt.RTContext;
import kvv.evlang.rt.UncaughtExceptionException;
import kvv.evlang.rt.VM;

class VM1 extends VM {

	private final IController controller;

	public VM1(RTContext cont, IController controller) throws UncaughtExceptionException {
		super(cont);
		this.controller = controller;
	}

	@Override
	public void setExtReg(int addr, int reg, int value) {
		try {
			controller.setReg(addr, reg, value);
		} catch (IOException e) {
		}
	}

	@Override
	public int getExtReg(int addr, int reg) {
		try {
			return controller.getReg(addr, reg);
		} catch (IOException e) {
		}
		return Const.INVALID_VALUE;
	}
}