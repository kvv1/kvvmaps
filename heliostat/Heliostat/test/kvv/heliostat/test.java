package kvv.heliostat;

import java.text.DateFormat;
import java.util.Calendar;

import kvv.heliostat.shared.math.MirrorAngles;

public class test {

	public static void main(String[] args) {

		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		calendar.set(Calendar.MONTH, 11);
		calendar.set(Calendar.DAY_OF_MONTH, 22);

		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

		for (int i = 0; i < 7; i++) {
			int d = 365 * i / 6 - 9;
			calendar.set(Calendar.DAY_OF_YEAR, d);
			System.out.println(d + " " + df.format(calendar.getTime()));
		}

		// int dayOfYear = 365 * 1 / 6 - 9;
		int dayOfYear = 0;

		double lat = 12.0066228;
		double lon = -79.8104811;
		double timezone = -5.5;

		double[] res = MirrorAngles.calcSun(dayOfYear, 18, lat, lon, timezone);

		System.out.printf("%.2f %.2f\n", res[0], res[1]);

	}

}
