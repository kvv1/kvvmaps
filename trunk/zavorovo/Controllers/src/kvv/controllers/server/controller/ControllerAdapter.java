package kvv.controllers.server.controller;

import java.io.IOException;
import java.io.InputStream;

import kvv.controllers.controller.IController;
import kvv.controllers.register.Rule;
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
	public void upload(int addr, byte[] data) throws IOException {
		wrapped.upload(addr, data);
	}

	@Override
	public void close() {
		stopped = true;
		wrapped.close();
	}

	@Override
	public void vmInit(int addr) throws IOException {
		wrapped.vmInit(addr);
	}

	@Override
	public String getStatistics(boolean clear) throws IOException {
		return wrapped.getStatistics(clear);
	}

	@Override
	public void uploadAppHex(int addr, InputStream is) throws IOException {
		wrapped.uploadAppHex(addr, is);
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
