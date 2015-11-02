package kvv.controllers.server.controller;

import java.io.IOException;
import java.io.InputStream;

import kvv.controller.register.Rule;
import kvv.controller.register.Statistics;
import kvv.controllers.controller.IController;
import kvv.controllers.controller.ModbusLine;
import kvv.controllers.server.Controllers;

public abstract class ControllerAdapter implements IController {

	protected volatile boolean stopped;

	protected final IController wrapped;
	protected final Controllers controllers;

	public ControllerAdapter(Controllers controllers, IController wrapped) {
		this.controllers = controllers;
		this.wrapped = wrapped;
	}

	@Override
	public void close() {
		stopped = true;
		wrapped.close();
	}

	@Override
	public void setRegs(int addr, int reg, int... val) throws IOException {
		wrapped.setRegs(addr, reg, val);
	}

	@Override
	public void setModbusLine(ModbusLine modbusLine) {
		wrapped.setModbusLine(modbusLine);
	}
	
	@Override
	public Statistics getStatistics(boolean clear) throws IOException {
		return wrapped.getStatistics(clear);
	}

	@Override
	public void uploadApp(int addr, byte[] data) throws IOException {
		wrapped.uploadApp(addr, data);
	}

	@Override
	public Integer hello(int addr) throws IOException {
		return wrapped.hello(addr);
	}

	@Override
	public Rule[] getRules(int addr) throws IOException {
		return wrapped.getRules(addr);
	}

	@Override
	public void setRules(int addr, Rule[] rules) throws IOException {
		wrapped.setRules(addr, rules);
	}
}
