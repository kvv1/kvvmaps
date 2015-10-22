package kvv.heliostat.server.envir;

import java.text.DateFormat;
import java.util.Calendar;

import kvv.heliostat.client.dto.DayTime;

public class SimTime implements Time {
	private Calendar calendar = Calendar.getInstance();
	{
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	@Override
	public DayTime getTime() {
		Calendar c = calendar;

		int ms = c.get(Calendar.HOUR_OF_DAY) * 3600000 + c.get(Calendar.MINUTE)
				* 60000 + c.get(Calendar.SECOND) * 1000
				+ c.get(Calendar.MILLISECOND);

		DayTime dayTime = new DayTime();
		dayTime.time = ms / 3600000d;
		dayTime.day = Math.min(365, c.get(Calendar.DAY_OF_YEAR) - 1);
		dayTime.dayS = df.format(c.getTime());
		dayTime.timeS = tf.format(c.getTime());
		return dayTime;
	}

	@Override
	public void setTime(double time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(calendar.getTimeInMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		calendar.setTimeInMillis((long) (c.getTimeInMillis() + time * 3600000));
	}

	@Override
	public void setDay(int day) {
		calendar.set(Calendar.DAY_OF_YEAR, day + 1);
	}

	public void step(int ms, boolean shortDay) {
		calendar.add(Calendar.MILLISECOND, ms);
		if (calendar.get(Calendar.HOUR_OF_DAY) >= 19) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 5);
		}
	}

	private final static DateFormat df = DateFormat
			.getDateInstance(DateFormat.MEDIUM);

	private final static DateFormat tf = DateFormat
			.getTimeInstance(DateFormat.LONG);

}
