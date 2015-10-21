package kvv.heliostat.server;

import kvv.gwtutils.client.login.AuthException;
import kvv.gwtutils.server.login.LoginServlet;
import kvv.heliostat.client.HeliostatServiceAux;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.dto.Weather;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class HeliostatServiceAuxImpl extends LoginServlet implements
		HeliostatServiceAux {
	@Override
	public void setClockRate(int value)  throws AuthException {
		checkUser();
		Heliostat.instance.setClockRate(value);
	}

	@Override
	public Weather getWeather() {
		return SimEnvironment.instance.getWeather();
	}

	@Override
	public void saveWeather(Weather weather)  throws AuthException {
		checkUser();
		try {
			SimEnvironment.instance.set(weather);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Weather resetSim(int firstDay)  throws AuthException {
		checkUser();
		SimEnvironment.instance.reset(firstDay);
		return SimEnvironment.instance.getWeather();
	}

	@Override
	public void shortDay(boolean value)  throws AuthException {
		checkUser();
		Heliostat.instance.shortDay(value);
	}

	@Override
	public void setRange(MotorId id, int max)  throws AuthException {
		checkUser();
		Heliostat.instance.setRange(id, max);
	}

}
