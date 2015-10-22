package kvv.heliostat.server.envir;

import java.text.DateFormat;
import java.util.Calendar;

import kvv.heliostat.client.dto.DayTime;

public class RealTime implements Time {

	@Override
	public DayTime getTime() {
		Calendar c = Calendar.getInstance();

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
	public void setDay(int day) {
		throw new IllegalStateException();
	}

	@Override
	public void setTime(double time) {
		throw new IllegalStateException();
	}

	private final static DateFormat df = DateFormat
			.getDateInstance(DateFormat.MEDIUM);

	private final static DateFormat tf = DateFormat
			.getTimeInstance(DateFormat.LONG);
}
