package kvv.heliostat.server;

import kvv.gwtutils.client.login.AuthException;
import kvv.gwtutils.server.login.LoginServlet;
import kvv.heliostat.client.HeliostatServiceAux;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.dto.Weather;
import kvv.heliostat.server.envir.Envir;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class HeliostatServiceAuxImpl extends LoginServlet implements
		HeliostatServiceAux {
	@Override
	public void setClockRate(int value) throws AuthException {
		checkUser();
		Heliostat.instance.setClockRate(value);
	}

	@Override
	public Weather getWeather() {
		return Envir.instance.weather;
	}

	@Override
	public void saveWeather(Weather weather) throws AuthException {
		checkUser();
		Envir.instance.weather = weather;
		Envir.instance.saveWeather();
	}

	@Override
	public Weather resetSim(int firstDay) throws AuthException {
		checkUser();
		Envir.instance.weather.firstDay = firstDay;
		Envir.instance.saveWeather();
		return Envir.instance.weather;
	}

	@Override
	public void shortDay(boolean value) throws AuthException {
		checkUser();
		Heliostat.instance.shortDay(value);
	}

	@Override
	public void setRange(MotorId id, int max) throws AuthException {
		checkUser();
		Heliostat.instance.setRange(id, max);
	}

	@Override
	public void setTime(double time)  throws AuthException {
		checkUser();
		Envir.instance.time.setTime(time);
	}

	@Override
	public void setDay(int day)  throws AuthException {
		checkUser();
		Envir.instance.time.setDay(day);
	}

}
