package kvv.controllers.controller;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import kvv.controllers.utils.Constants;

public class BusLogger {
	private static Set<Integer> failedAddrs = new HashSet<Integer>();

	public static synchronized void logSuccess(int addr) {
		if (failedAddrs.contains(addr))
			log("addr=" + addr + " OK");
		failedAddrs.remove(addr);
	}

	public static synchronized void logErr(int addr, String msg) {
		if (!failedAddrs.contains(addr))
			log("addr=" + addr + " " + msg);
		failedAddrs.add(addr);
	}

	private static final DateFormat df = new SimpleDateFormat(
			"yyyy.MM.dd HH:mm:ss");

	public static void log(String txt) {
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(Constants.rs485LogFile,
					true), true, "Windows-1251");
			ps.println(df.format(new Date()) + " " + txt);
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
