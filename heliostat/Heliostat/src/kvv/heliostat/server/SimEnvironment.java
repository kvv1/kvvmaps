package kvv.heliostat.server;

import java.io.IOException;

import kvv.gwtutils.server.Utils;
import kvv.heliostat.shared.Weather;

public class SimEnvironment {
	private static final String WEATHER_FILE = "c:/heliostat/weather.json";

	public Weather weather;

	public SimEnvironment() {
		try {
			weather = Utils.jsonRead(WEATHER_FILE, Weather.class);
		} catch (IOException e) {
			weather = new Weather(5, 19, 4, 0, new boolean[30][(19 - 5) * 4]);
		}
	}

	public void set(Weather weather2) throws Exception {
		weather = weather2;
		Utils.jsonWrite(WEATHER_FILE, weather);
	}

	public boolean isSunny() {
		int dayOffset = weather.d2off(Time.getDay());

		try {
			return weather.values[dayOffset][weather.t2p(Time.getTime())];
		} catch (Exception e) {
			return false;
		}

	}

	public void reset(int firstDay) {
		weather.firstDay = firstDay;
		try {
			Utils.jsonWrite(WEATHER_FILE, weather);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
