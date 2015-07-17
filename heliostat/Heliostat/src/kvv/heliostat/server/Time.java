package kvv.heliostat.server;

import java.text.DateFormat;
import java.util.Calendar;

public class Time {
	private static Calendar calendar = Calendar.getInstance();
	static {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	private static boolean simTime = true;

	public static double getTime() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(calendar.getTimeInMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return (calendar.getTimeInMillis() - c.getTimeInMillis()) / 3600000d;
	}

	public static void setTime(double time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(calendar.getTimeInMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		calendar.setTimeInMillis((long) (c.getTimeInMillis() + time * 3600000));
	}

	public static int getDay() {
		return Math.min(365, calendar.get(Calendar.DAY_OF_YEAR));
	}

	public static Calendar getCalendar() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(calendar.getTimeInMillis());
		return c;
	}

	public static void setDay(int day) {
		calendar.set(Calendar.DAY_OF_YEAR, day);
	}

	public static void step(int ms, boolean shortDay) {
		if (simTime) {
			calendar.add(Calendar.MILLISECOND, ms);
			if (calendar.get(Calendar.HOUR_OF_DAY) >= 19) {
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				calendar.set(Calendar.HOUR_OF_DAY, 5);
			}
		} else {
			calendar = Calendar.getInstance();
		}
	}

	private final static DateFormat df = DateFormat
			.getDateInstance(DateFormat.MEDIUM);

	private final static DateFormat tf = DateFormat
			.getTimeInstance(DateFormat.LONG);

	public static String getDayS() {
		return df.format(calendar.getTime());
	}

	public static String getTimeS() {
		return tf.format(calendar.getTime());
	}

}
