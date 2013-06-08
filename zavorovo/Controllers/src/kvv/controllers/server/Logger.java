package kvv.controllers.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerDescr.Type;
import kvv.controllers.shared.Register;
import kvv.controllers.utils.Constants;

public class Logger extends Thread {
	public static volatile Logger instance;
	public volatile boolean stopped;

	{
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

	@Override
	public void run() {
		while (!stopped) {
			try {
				ControllerDescr[] controllers = Utils.jsonRead(
						Constants.controllersFile, ControllerDescr[].class);

				for (ControllerDescr c : controllers) {
					if (c != null) {
						Thread.sleep(1000);
						if (c.type == Type.MU110_8)
							ControllersServiceImpl.controller.getRegs(c.addr,
									0, 8);
						else
							ControllersServiceImpl.controller
									.getAllRegs(c.addr);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static PrintStream getLogStream() {
		Date date = new Date();
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);

		try {
			new File(Constants.ROOT + "/history").mkdir();
			DateFormat df = new SimpleDateFormat("yyyy_MM_dd");
			return new PrintStream(new FileOutputStream(Constants.ROOT
					+ "/history/" + df.format(date), true), true,
					"Windows-1251");
		} catch (Exception e) {
			return null;
		}
	}

	static DateFormat df = new SimpleDateFormat("yyyy_MM_dd");

	public static void main(String[] args) {
		Date date = new Date();

		System.out.println(df.format(date));

	}

	private static void logMap(int addr, Map<Integer, Integer> map) {
		PrintStream ps = getLogStream();
		if (ps == null)
			return;

		if (map == null)
			ps.println(df.format(new Date()) + " " + addr);
		else {
			ps.print(df.format(new Date()) + " " + addr);
			for (Integer a : map.keySet())
				ps.print(" " + a + "=" + map.get(a));
			ps.println();
		}

		ps.close();
	}

	static Map<Integer, Map<Integer, Integer>> lastValues = new HashMap<Integer, Map<Integer, Integer>>();

	public static void log(int addr, Map<Integer, Integer> newMap) {
		if (newMap != null) {
			Map<String, Register> regs = Controllers.getRegisters();
			List<Integer> arr = new ArrayList<Integer>();
			for (Register r : regs.values())
				arr.add(r.register);
			newMap.keySet().retainAll(arr);
		}

//		if (newMap != null && newMap.isEmpty()) {
//			return;
//		}

		Map<Integer, Integer> lastMap = lastValues.get(addr);

		if (newMap == null && lastMap != null) {
			logMap(addr, null);
			lastValues.put(addr, null);
		} else if (newMap != null && lastMap == null) {
			logMap(addr, newMap);
			lastValues.put(addr, newMap);
		} else if (newMap != null) {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (Integer a : newMap.keySet())
				if (!newMap.get(a).equals(lastMap.get(a))) {
					map.put(a, newMap.get(a));
					lastMap.put(a, newMap.get(a));
				}
			if (!map.isEmpty())
				logMap(addr, map);
		}
	}

	public static void log(int addr, int reg, int val) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(reg, val);
		log(addr, map);
	}

}
