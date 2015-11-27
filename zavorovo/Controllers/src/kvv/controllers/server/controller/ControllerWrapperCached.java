package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;

public class ControllerWrapperCached extends ControllerAdapter {

	private Map<Integer, HashMap<Integer, Integer>> map = new HashMap<>();

	private final Runnable r = new Runnable() {
		private List<ControllerDescr> controllersList;

		@Override
		public void run() {
			if (controllersList == null || controllersList.isEmpty())
				controllersList = new LinkedList<ControllerDescr>(
						Arrays.asList(controllers.getControllers()));

			while (!controllersList.isEmpty()) {
				ControllerDescr controllerDescr = controllersList.remove(0);
				if (!controllerDescr.enabled)
					continue;

				int addr = controllerDescr.addr;

				try {
					int[] regRange = getRegRange(addr);
					if (regRange != null) {
						HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
						int[] vals = wrapped.getRegs(addr, regRange[0],
								regRange[1]);
						for (int i = 0; i < regRange[1]; i++)
							map.put(regRange[0] + i, vals[i]);
						ControllerWrapperCached.this.map.put(addr, map);
					}
					break;
				} catch (IOException e) {
					map.remove(addr);
				}
			}
			Context.looper.post(this, 100);
		}
	};

	public ControllerWrapperCached(Controllers controllers,
			IController controller) {
		super(controllers, controller);
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
	public int getReg(int addr, int reg) throws IOException {
		HashMap<Integer, Integer> allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException("Не найден контроллер с адресом " + addr);
		Integer val = allRegs.get(reg);
		if (val == null)
			throw new IOException("Не найдено значение регистра " + reg
					+ "контроллера " + addr);
		return val;
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		HashMap<Integer, Integer> allRegs = map.get(addr);
		if (allRegs == null)
			throw new IOException();

		int[] res = new int[n];
		for (int i = 0; i < n; i++) {
			Integer val = allRegs.get(reg + i);
			if (val == null)
				val = 0;
			res[i] = val;
		}
		return res;
	}

	private final static int[] globalsRegRange = { 0, 256 };
	
	private int[] getRegRange(int addr) throws IOException {
		if (addr == 0)
			return globalsRegRange;
		ControllerDescr controllerDescr = controllers.get(addr);
		ControllerType controllerType = controllers.getControllerTypes().get(
				controllerDescr.type);
		if (controllerType == null)
			throw new ControllerTypeNotFoundException(controllerDescr.type);
		return controllerType.def.allRegs;
	}

	public HashMap<Integer, Integer> getAllRegs(int addr) {
		return map.get(addr);
	}
}
