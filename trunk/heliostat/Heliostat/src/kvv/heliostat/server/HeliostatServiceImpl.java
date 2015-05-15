package kvv.heliostat.server;

import kvv.heliostat.client.HeliostatService;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorId;
import kvv.heliostat.shared.Params.AutoMode;
import kvv.heliostat.shared.Weather;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class HeliostatServiceImpl extends RemoteServiceServlet implements
		HeliostatService {

	public String greetServer(String input) throws IllegalArgumentException {
		return input;
	}

	@Override
	public HeliostatState getState() {
		return Heliostat.instance.getState();
	}

	@Override
	public void setAuto(AutoMode auto) {
		Heliostat.instance.setAuto(auto);
	}

	@Override
	public void home() {
		Heliostat.instance.home();
	}

	@Override
	public void setTime(double time) {
		Heliostat.instance.setTime(time);
	}

	@Override
	public void clock(boolean value) {
		Heliostat.instance.setClock(value);
	}

	@Override
	public void move(MotorId id, int pos) {
		Heliostat.instance.move(id, pos);
	}

	@Override
	public void stop(MotorId id) {
		Heliostat.instance.stop(id);
	}

	@Override
	public void home(MotorId id) {
		Heliostat.instance.home(id);
	}

	@Override
	public void moveRaw(MotorId id, int steps) {
		Heliostat.instance.moveRaw(id, steps);
	}

	@Override
	public void calibrate(MotorId id) {
		Heliostat.instance.calibrate(id);
	}

	@Override
	public void setStepsPerDegree(MotorId id, int value) {
		Heliostat.instance.setStepsPerDegree(id, value);
	}

	@Override
	public void setClockRate(int value) {
		Heliostat.instance.setClockRate(value);
	}

	@Override
	public void setStepMS(int value) {
		Heliostat.instance.setStepMS(value);
	}

	@Override
	public void setDay(int day) {
		Heliostat.instance.setDay(day);
	}

	@Override
	public void clearHistory() {
		Heliostat.instance.clearHistory();
	}

	@Override
	public Weather getWeather() {
		return Heliostat.instance.getWeather();
	}

	@Override
	public void saveWeather(Weather weather) {
		Heliostat.instance.saveWeather(weather);
	}

	@Override
	public Weather resetSim(int firstDay) {
		return Heliostat.instance.resetSim(firstDay);
	}

	@Override
	public void shortDay(boolean value) {
		Heliostat.instance.shortDay(value);
	}

}
