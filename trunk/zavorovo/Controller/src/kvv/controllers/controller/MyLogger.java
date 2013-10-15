package kvv.controllers.controller;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import kvv.controllers.utils.Constants;

public class MyLogger {
	private static final DateFormat df = new SimpleDateFormat(
			"yyyy.MM.dd HH:mm:ss");

	public static synchronized void log(String txt) {
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(Constants.logFile, true),
					true, "Windows-1251");
			ps.println(df.format(new Date()) + " " + txt);
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
