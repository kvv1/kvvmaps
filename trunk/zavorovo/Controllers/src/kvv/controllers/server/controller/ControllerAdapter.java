package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Map;

import kvv.controllers.controller.IController;
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
	public Map<Integer, Statistics> getStatistics() {
		return wrapped.getStatistics();
	}

	@Override
	public void clearStatistics() {
		wrapped.clearStatistics();
	}

}
