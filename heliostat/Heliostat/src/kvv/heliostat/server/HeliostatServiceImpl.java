package kvv.heliostat.server;

import kvv.gwtutils.client.login.AuthException;
import kvv.gwtutils.server.login.LoginServlet;
import kvv.heliostat.client.HeliostatService;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorId;
import kvv.heliostat.shared.Params.AutoMode;
import kvv.heliostat.shared.Weather;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class HeliostatServiceImpl extends LoginServlet implements
		HeliostatService {

	public String greetServer(String input) throws IllegalArgumentException {
		return input;
	}

	@Override
	public HeliostatState getState() {
		return Heliostat.instance.getState();
	}

	@Override
	public void setAuto(AutoMode auto) throws AuthException {
		checkUser();
		Heliostat.instance.setAuto(auto);
	}

	@Override
	public void home() throws AuthException {
		checkUser();
		Heliostat.instance.home();
	}

	@Override
	public void setTime(double time)  throws AuthException {
		checkUser();
		Heliostat.instance.setTime(time);
	}

	@Override
	public void clock(boolean value)  throws AuthException {
		checkUser();
		Heliostat.instance.setClock(value);
	}

	@Override
	public void move(MotorId id, int pos)  throws AuthException {
		checkUser();
		Heliostat.instance.move(id, pos);
	}

	@Override
	public void stop(MotorId id)  throws AuthException {
		checkUser();
		Heliostat.instance.stop(id);
	}

	@Override
	public void home(MotorId id)  throws AuthException {
		checkUser();
		Heliostat.instance.home(id);
	}

	@Override
	public void moveRaw(MotorId id, int steps)  throws AuthException {
		checkUser();
		Heliostat.instance.moveRaw(id, steps);
	}

	@Override
	public void setStepsPerDegree(MotorId id, int value)  throws AuthException {
		checkUser();
		Heliostat.instance.setStepsPerDegree(id, value);
	}

	@Override
	public void setClockRate(int value)  throws AuthException {
		checkUser();
		Heliostat.instance.setClockRate(value);
	}

	@Override
	public void setStepMS(int value)  throws AuthException {
		checkUser();
		Heliostat.instance.setStepMS(value);
	}

	@Override
	public void setDay(int day)  throws AuthException {
		checkUser();
		Heliostat.instance.setDay(day);
	}

	@Override
	public void clearHistory()  throws AuthException {
		checkUser();
		Heliostat.instance.clearHistory();
	}

	@Override
	public Weather getWeather() {
		return Heliostat.instance.getWeather();
	}

	@Override
	public void saveWeather(Weather weather)  throws AuthException {
		checkUser();
		Heliostat.instance.saveWeather(weather);
	}

	@Override
	public Weather resetSim(int firstDay)  throws AuthException {
		checkUser();
		return Heliostat.instance.resetSim(firstDay);
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

	@Override
	public void setControllerParams(String str) throws AuthException {
		checkUser();
		Heliostat.instance.setControllerParams(str);
	}

}
