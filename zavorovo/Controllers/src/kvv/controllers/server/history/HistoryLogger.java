package kvv.controllers.server.history;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kvv.controllers.history.HistoryFile;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.ControllersServiceImpl;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerDescr.Type;
import kvv.controllers.shared.Register;

public class HistoryLogger extends Thread {
	public static volatile HistoryLogger instance;
	public volatile boolean stopped;

	public static synchronized void stopLogger() {
		if (instance == null)
			return;
		instance.stopped = true;
	}

	{
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

	@Override
	public void run() {
		synchronized (getClass()) {
			HistoryFile.logValue(new Date(), null, null);
		}
		while (!stopped) {
			try {
				ControllerDescr[] controllers = Controllers.getInstance()
						.getControllers();
				for (ControllerDescr c : controllers) {
					if (stopped)
						break;
					try {
						Thread.sleep(500);
						if (c.type == Type.MU110_8)
							ControllersServiceImpl.controller.getRegs(c.addr,
									0, 8);
						else
							ControllersServiceImpl.controller
									.getAllRegs(c.addr);
					} catch (Exception e) {
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		synchronized (getClass()) {
			HistoryFile.logValue(new Date(), null, null);
			instance = null;
		}
	}

	private static Map<String, Integer> lastValues = new HashMap<String, Integer>();
	private static Date lastFileDate;

	@SuppressWarnings("deprecation")
	private void logValue(String register, Integer value) {
		Date date = new Date();
		// Date fileDate = new Date(date.getTime());
		Date fileDate = new Date(date.getYear(), date.getMonth(),
				date.getDate());
		fileDate.setHours(0);
		fileDate.setMinutes(0);
		fileDate.setSeconds(0);

		// date = new Date(date.getYear(), date.getMonth(), date.getDate());

		if (!fileDate.equals(lastFileDate)) {
			logValues(fileDate, lastValues);
			lastFileDate = fileDate;
		}

		Integer lastValue = lastValues.get(register);
		if (value == null && lastValue != null || value != null
				&& !value.equals(lastValue))
			logValue(date, register, value);

	}

	private void logValue(Date date, String register, Integer value) {
		HistoryFile.logValue(date, register, value);
		lastValues.put(register, value);
	}

	private void logValues(Date date, Map<String, Integer> values) {
		for (String reg : values.keySet())
			logValue(date, reg, values.get(reg));
	}

	public static synchronized void log(int addr, int reg, Integer val) {
		if (instance == null)
			return;
		Register register = Controllers.getInstance().getRegister(addr, reg);
		if (register == null)
			return;
		instance.logValue(register.name, val);
	}

	public static synchronized void log(int addr, Map<Integer, Integer> values) {
		if (instance == null)
			return;
		if (values == null) {
			Collection<Register> regs = Controllers.getInstance().getRegisters(
					addr);
			for (Register reg : regs)
				instance.logValue(reg.name, null);
			return;
		}
		for (int reg : values.keySet())
			log(addr, reg, values.get(reg));
	}

}
