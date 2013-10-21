package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.server.Controllers;
import kvv.controllers.shared.ControllerDescr;
import kvv.evlang.rt.Const;

class ControllerWrapperCached extends Controller {

	private Map<Integer, AllRegs> map = new HashMap<Integer, AllRegs>();

	public ControllerWrapperCached(IController controller) {
		super(controller);
		thread.start();
	}

	@Override
	public synchronized void setReg(int addr, int reg, int val)
			throws IOException {
		AllRegs allRegs = map.get(addr);
		try {
			wrapped.setReg(addr, reg, val);
			if (allRegs != null)
				allRegs.values.put(reg, val);
		} catch (IOException e) {
			if (allRegs != null)
				allRegs.values.put(reg, null);
		}
	}

	@Override
	public synchronized int getReg(int addr, int reg) throws IOException {
		AllRegs allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException();
		Integer val = allRegs.values.get(reg);
		if (val == null)
			throw new IOException();
		return val;
	}

	@Override
	public synchronized int[] getRegs(int addr, int reg, int n)
			throws IOException {
		AllRegs allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException();

		int[] res = new int[n];
		for (int i = 0; i < n; i++) {
			Integer val = allRegs.values.get(reg + i);
			if (val == null)
				val = Const.INVALID_VALUE;
			res[i] = val;
		}
		return res;
	}

	@Override
	public synchronized AllRegs getAllRegs(int addr) throws IOException {
		AllRegs allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException();
		return allRegs;
	}

	private List<ControllerDescr> controllers;

	private void step() {

		if (controllers == null || controllers.isEmpty())
			controllers = new LinkedList<ControllerDescr>(
					Arrays.asList(Controllers.getInstance().getControllers()));

		if (!controllers.isEmpty()) {
			int addr = controllers.remove(0).addr;
			try {
				AllRegs allRegs = wrapped.getAllRegs(addr);
				synchronized (this) {
					map.put(addr, allRegs);
				}
			} catch (IOException e) {
				synchronized (this) {
					map.remove(addr);
				}
			}
		}
	}

	private Thread thread = new Thread(
			ControllerWrapperCached.class.getSimpleName() + "Thread") {
		{
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			while (!stopped) {
				try {
					Thread.sleep(100);
					step();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

}
