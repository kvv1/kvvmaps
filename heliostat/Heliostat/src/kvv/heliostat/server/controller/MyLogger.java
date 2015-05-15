package kvv.heliostat.server.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyLogger {
	private static final DateFormat df = new SimpleDateFormat(
			"yyyy.MM.dd HH:mm:ss");

	public static synchronized void log(String txt) {
		System.out.println(df.format(new Date()) + " " + txt);
//		PrintStream ps = null;
//		try {
//			ps = new PrintStream(new FileOutputStream(Constants.logFile, true),
//					true, "Windows-1251");
//			ps.println(df.format(new Date()) + " " + txt);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (ps != null)
//				ps.close();
//		}
	}
}
