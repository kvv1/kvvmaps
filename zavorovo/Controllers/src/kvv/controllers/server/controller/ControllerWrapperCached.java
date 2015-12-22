package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Logger;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.history.HistoryFile;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.SystemDescr;

public class ControllerWrapperCached extends ControllerAdapter {

	private final Cache cache = new Cache();

	private final Runnable r = new Runnable() {
		private List<ControllerDescr> controllersList;

		int day = -1;

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			try {
				Date date = new Date();
				if (date.getDate() != day) {
					cache.logAll();
					day = date.getDate();
				}

				if (controllersList == null || controllersList.isEmpty()) {
					controllersList = new LinkedList<ControllerDescr>(
							Arrays.asList(system.controllers));
				}

				while (!controllersList.isEmpty()) {
					ControllerDescr controllerDescr = controllersList.remove(0);
					if (!controllerDescr.enabled)
						continue;
					updateCache(controllerDescr);
					break;
				}

			} catch (Exception e) {
				e.printStackTrace(Logger.out);
			}
			Context.looper.post(this, 10);
		}

	};

	private void updateCache(ControllerDescr cd) {
		try {
			int[] regRange = getRegsRange(cd);
			if (regRange != null) {
				Integer[] vals = wrapped.getRegs(cd.addr, regRange[0],
						regRange[1]);
				cache.put(cd.addr, regRange[0], vals);
			}
		} catch (IOException e) {
			cache.remove(cd.addr);
		}
	}

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
		// HashMap<Integer, Integer> allRegs = cache.get(addr);
		try {
			wrapped.setReg(addr, reg, val);
			cache.put(addr, reg, val);
		} catch (IOException e) {
			cache.remove(addr, reg);
			throw e;
		}
	}

	@Override
	public Integer getReg(int addr, int reg) throws IOException {
		HashMap<Integer, Integer> allRegs = cache.get(addr);
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
		HashMap<Integer, Integer> allRegs = cache.get(addr);
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

	public HashMap<Integer, Integer> getCachedRegs(int addr, boolean updateCache) {
		if (updateCache) {
			ControllerDescr cd = system.getController(addr);
			if (cd == null)
				return null;
			if (!cd.enabled)
				return null;
			updateCache(cd);
		}
		return cache.get(addr);
	}

	class Cache {
		private Map<Integer, HashMap<Integer, Integer>> map = new HashMap<>();

		public HashMap<Integer, Integer> get(int addr) {
			return map.get(addr);
		}

		public void logAll() {
			for (int addr : map.keySet())
				for (int reg : map.get(addr).keySet())
					log(addr, reg, map.get(addr).get(reg));
		}

		public void put(int addr, int reg, Integer[] vals) {
			HashMap<Integer, Integer> map1 = get(addr);
			if (map1 == null)
				map1 = new HashMap<Integer, Integer>();
			for (int i = 0; i < vals.length; i++) {
				if (!eq(map1.get(reg + i), vals[i]))
					log(addr, reg + i, vals[i]);
				map1.put(reg + i, vals[i]);
			}
			map.put(addr, map1);

		}

		public void put(int addr, int reg, Integer val) {
			HashMap<Integer, Integer> allRegs = get(addr);
			if (allRegs == null)
				return;

			if (!allRegs.containsKey(reg))
				return;

			if (!eq(allRegs.get(reg), val))
				log(addr, reg, val);

			allRegs.put(reg, val);
		}

		public void remove(int addr, int reg) {
			HashMap<Integer, Integer> allRegs = get(addr);
			if (allRegs != null)
				allRegs.put(reg, null);
			log(addr, reg, null);
		}

		public void remove(int addr) {
			HashMap<Integer, Integer> map1 = map.remove(addr);
			if (map1 != null)
				for (Integer reg : map1.keySet())
					log(addr, reg, null);
		}

		private void log(int addr, int reg, Integer val) {
			RegisterDescr register = system.getRegister(addr, reg);
			if (register == null)
				return;
			HistoryFile.logValue(register.name, val);
		}

		private boolean eq(Integer i1, Integer i2) {
			if (i1 == null && i2 == null)
				return true;
			if (i1 != null && i2 != null)
				return i1.intValue() == i2.intValue();
			return false;
		}
	}

}
