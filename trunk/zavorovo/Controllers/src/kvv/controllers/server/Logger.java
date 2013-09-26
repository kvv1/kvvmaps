package kvv.controllers.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
					try {
						if (c != null) {
							Thread.sleep(1000);
							if (c.type == Type.MU110_8)
								ControllersServiceImpl.controller.getRegs(
										c.addr, 0, 8);
							else
								ControllersServiceImpl.controller
										.getAllRegs(c.addr);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static File getLogFile(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy_MM_dd");
		return new File(Constants.ROOT + "/history/" + df.format(date));
	}

	private static PrintStream getLogStream(Date date) {
		try {
			new File(Constants.ROOT + "/history").mkdir();
			PrintStream ps = new PrintStream(new FileOutputStream(
					getLogFile(date), true), true, "Windows-1251");
			return ps;
		} catch (Exception e) {
			return null;
		}
	}

	private static DateFormat df = new SimpleDateFormat("HH:mm:ss");
	private static Map<String, Integer> lastValues = new HashMap<String, Integer>();
	private static Date lastFileDate;

	public static void main(String[] args) {
		Date date = new Date();

		System.out.println(df.format(date));

	}

	@SuppressWarnings("deprecation")
	private static synchronized void logValue(String register, Integer value) {
		Date date = new Date();
		// Date fileDate = new Date(date.getTime());
		Date fileDate = new Date(date.getYear(), date.getMonth(),
				date.getDate());
		fileDate.setHours(0);
		fileDate.setMinutes(0);
		fileDate.setSeconds(0);

		// date = new Date(date.getYear(), date.getMonth(), date.getDate());

		if (!fileDate.equals(lastFileDate)) {
			PrintStream ps = getLogStream(fileDate);
			logValues(ps, fileDate, lastValues);
			ps.close();
			lastFileDate = fileDate;
		}

		Integer lastValue = lastValues.get(register);
		if (value == null && lastValue != null || value != null
				&& !value.equals(lastValue)) {
			PrintStream ps = getLogStream(fileDate);
			logValue(ps, date, register, value);
			ps.close();
		}

	}

	private static void logValue(PrintStream ps, Date date, String register,
			Integer value) {
		if (value == null)
			ps.println(df.format(date) + " " + register);
		else {
			ps.println(df.format(date) + " " + register + "=" + value);
		}
		lastValues.put(register, value);
	}

	private static void logValues(PrintStream ps, Date date,
			Map<String, Integer> values) {
		for (String reg : values.keySet())
			logValue(ps, date, reg, values.get(reg));
	}

	public static void log(int addr, int reg, int val) {
		Register register = Controllers.getRegister(addr, reg);
		if (register == null)
			return;
		logValue(register.name, val);
	}

	public static void log(int addr, Map<Integer, Integer> values) {
		if (values == null) {
			Collection<Register> regs = Controllers.getRegisters(addr);
			for (Register reg : regs)
				logValue(reg.name, null);
			return;
		}
		for (int reg : values.keySet())
			log(addr, reg, values.get(reg));
	}

}
