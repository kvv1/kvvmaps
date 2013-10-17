package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kvv.controllers.controller.IController;
import kvv.controllers.history.HistoryFile;
import kvv.controllers.register.AllRegs;
import kvv.controllers.server.Controllers;
import kvv.controllers.shared.ControllerDescr;

class ControllerWrapperCached implements IController {

	private final IController controller;

	private Map<Integer, AllRegs> map = new HashMap<Integer, AllRegs>();

	// private AllRegs allRegs(int addr) throws IOException {
	// AllRegs allRegs = map.get(addr);
	// if (allRegs == null) {
	// allRegs = controller.getAllRegs(addr);
	// map.put(addr, allRegs);
	// }
	// return allRegs;
	// }

	public ControllerWrapperCached(IController controller) {
		this.controller = controller;
	}

	@Override
	public synchronized void setReg(int addr, int reg, int val)
			throws IOException {
		AllRegs allRegs = map.get(addr);
		try {
			controller.setReg(addr, reg, val);
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
		return allRegs.values.get(reg);
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
				val = 0;
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

	@Override
	public void upload(int addr, byte[] data) throws IOException {
		controller.upload(addr, data);
	}

	@Override
	public void close() {
		stopped = true;
		controller.close();
	}

	@Override
	public void vmInit(int addr) throws IOException {
		controller.vmInit(addr);
	}

	private synchronized void refreshCache(int addr) {
		try {
			map.put(addr, controller.getAllRegs(addr));
		} catch (IOException e) {
			map.remove(addr);
		}
	}

	private volatile boolean stopped;

	private Thread thread = new Thread() {

		{
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
			start();
		}

		@Override
		public void run() {
			HistoryFile.logValue(new Date(), null, null);

			while (!stopped) {
				try {
					Thread.sleep(100);
					ControllerDescr[] controllers = Controllers.getInstance()
							.getControllers();
					for (ControllerDescr c : controllers) {
						if (stopped)
							break;
						try {
							Thread.sleep(100);
							synchronized (ControllerWrapperCached.this) {
								if (!stopped)
									refreshCache(c.addr);
							}
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			HistoryFile.logValue(new Date(), null, null);
		}
	};

}
