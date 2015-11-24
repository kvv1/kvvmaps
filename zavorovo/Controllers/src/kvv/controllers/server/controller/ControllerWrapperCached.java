package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kvv.controller.register.AllRegs;
import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.ControllerDescr;

public class ControllerWrapperCached extends ControllerAdapter {

	private Map<Integer, AllRegs> map = new HashMap<Integer, AllRegs>();

	private final Runnable r = new Runnable() {
		@Override
		public void run() {
//			System.out.print("+");
			if (controllersList == null || controllersList.isEmpty())
				controllersList = new LinkedList<ControllerDescr>(
						Arrays.asList(controllers.getControllers()));

			while (!controllersList.isEmpty()) {
				ControllerDescr controllerDescr = controllersList.remove(0);
				int addr = controllerDescr.addr;
				if (!controllerDescr.enabled)
					continue;

				try {
					AllRegs allRegs = wrapped.getAllRegs(addr);
					map.put(addr, allRegs);
					break;
				} catch (IOException e) {
					map.remove(addr);
				}
			}
			Context.looper.post(this, 100);
//			System.out.print("-");
		}
	};

	public ControllerWrapperCached(Controllers controllers,
			IController controller) {
		super(controllers, controller);
		// thread.start();
		Context.looper.post(r, 100);
	}

	@Override
	public void close() {
		super.close();
		Context.looper.remove(r);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		// System.out.println("+" + addr + "(" + reg + ")=" + val);
		AllRegs allRegs = map.get(addr);
		try {
			wrapped.setReg(addr, reg, val);
			if (allRegs != null)
				allRegs.values.put(reg, val);
		} catch (IOException e) {
			if (allRegs != null)
				allRegs.values.put(reg, null);
			throw e;
		}
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		AllRegs allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException("Не найден контроллер с адресом " + addr);
		Integer val = allRegs.values.get(reg);
		if (val == null)
			throw new IOException("Не найдено значение регистра " + reg
					+ "контроллера " + addr);
		return val;
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
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
	public AllRegs getAllRegs(int addr) throws IOException {
		AllRegs allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException();
		return allRegs;
	}

	private List<ControllerDescr> controllersList;

	// private void step() {
	//
	// }

	// private Thread thread = new Thread(
	// ControllerWrapperCached.class.getSimpleName() + "Thread") {
	// {
	// setDaemon(true);
	// setPriority(Thread.MIN_PRIORITY);
	// }
	//
	// @Override
	// public void run() {
	// while (!stopped) {
	// try {
	// Thread.sleep(100);
	// step();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// };

}
