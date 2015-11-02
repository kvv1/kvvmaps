package kvv.heliostat.server;

import kvv.gwtutils.client.login.AuthException;
import kvv.gwtutils.server.login.LoginServlet;
import kvv.heliostat.client.HeliostatServiceAux;
import kvv.heliostat.client.dto.Weather;
import kvv.heliostat.server.envir.Envir;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class HeliostatServiceAuxImpl extends LoginServlet implements
		HeliostatServiceAux {

	// @Override
	// protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
	// throws ServletException, IOException {
	// synchronized (Heliostat.instance) {
	// super.service(arg0, arg1);
	// }
	// }

	@Override
	public void setClockRate(int value) throws AuthException {
		checkUser();
		ParamsHolder.params.simParams.clockRate = value;
		ParamsHolder.writeParams();
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
		ParamsHolder.params.simParams.shortDay = value;
		ParamsHolder.writeParams();
	}

	@Override
	public void setTime(double time) throws AuthException {
		checkUser();
		Envir.instance.time.setTime(time);
	}

	@Override
	public void setDay(int day) throws AuthException {
		checkUser();
		Envir.instance.time.setDay(day);
	}

	@Override
	public void setSim(boolean value) throws AuthException {
		checkUser();
		ParamsHolder.params.SIM = value;
		ParamsHolder.writeParams();
		Envir.recreate();
	}

}
