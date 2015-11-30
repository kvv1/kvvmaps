package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kvv.controllers.controller.IController;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.SystemDescr;

public class ControllerWrapperCached extends ControllerAdapter {

	private Map<Integer, HashMap<Integer, Integer>> map = new HashMap<>();

	private final Runnable r = new Runnable() {
		private List<ControllerDescr> controllersList;

		@Override
		public void run() {
			if (controllersList == null || controllersList.isEmpty())
				controllersList = new LinkedList<ControllerDescr>(
						Arrays.asList(system.controllers));

			while (!controllersList.isEmpty()) {
				ControllerDescr controllerDescr = controllersList.remove(0);
				if (!controllerDescr.enabled)
					continue;
				try {
					int[] regRange = getRegsRange(controllerDescr);
					if (regRange != null) {
						HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
						Integer[] vals = wrapped.getRegs(controllerDescr.addr,
								regRange[0], regRange[1]);
						for (int i = 0; i < regRange[1]; i++)
							map.put(regRange[0] + i, vals[i]);
						ControllerWrapperCached.this.map.put(
								controllerDescr.addr, map);
					}
				} catch (IOException e) {
					map.remove(controllerDescr.addr);
				}
				break;
			}
			Context.looper.post(this, 10);
		}
	};

	public ControllerWrapperCached(SystemDescr system, IController controller) {
		super(system, controller);
		Context.looper.post(r, 100);
	}

	@Override
	public void close() {
		super.close();
		Context.looper.remove(r);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		HashMap<Integer, Integer> allRegs = map.get(addr);
		try {
			wrapped.setReg(addr, reg, val);
			if (allRegs != null)
				allRegs.put(reg, val);
		} catch (IOException e) {
			if (allRegs != null)
				allRegs.put(reg, null);
			throw e;
		}
	}

	@Override
	public Integer getReg(int addr, int reg) throws IOException {
		HashMap<Integer, Integer> allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException("Контроллер с адресом " + addr
					+ " недоступен");

		if (allRegs.containsKey(reg))
			return allRegs.get(reg);
		else
			return wrapped.getReg(addr, reg);
	}

	@Override
	public Integer[] getRegs(int addr, int reg, int n) throws IOException {
		HashMap<Integer, Integer> allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException();

		Integer[] res = new Integer[n];
		for (int i = 0; i < n; i++) {
			Integer val = allRegs.get(reg + i);
			res[i] = val;
		}
		return res;
	}

	private final static int[] globalsRegRange = { 0, 256 };

	private int[] getRegsRange(ControllerDescr controllerDescr) {
		if (controllerDescr.addr == 0)
			return globalsRegRange;
		ControllerType controllerType = system.controllerTypes
				.get(controllerDescr.type);
		if (controllerType == null)
			return null;
		return controllerType.def.allRegs;
	}

	public HashMap<Integer, Integer> getCachedRegs(int addr) {
		return map.get(addr);
	}
}
