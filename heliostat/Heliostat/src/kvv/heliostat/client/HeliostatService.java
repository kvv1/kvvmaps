package kvv.heliostat.client;

import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorId;
import kvv.heliostat.shared.Params.AutoMode;
import kvv.heliostat.shared.Weather;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("heliostat")
public interface HeliostatService extends RemoteService {
	String greetServer(String name) throws IllegalArgumentException;

	HeliostatState getState();

	void move(MotorId id, int pos);

	void stop(MotorId id);

	void home(MotorId id);

	void moveRaw(MotorId id, int steps);

	void calibrate(MotorId id);

	void setAuto(AutoMode auto);

	void home();

	void setTime(double time);

	void clock(boolean value);

	void setStepsPerDegree(MotorId id, int value);

	void setClockRate(int value);

	void setStepMS(int value);

	void setDay(int day);

	void clearHistory();

	Weather getWeather();
	
	void saveWeather(Weather weather);

	Weather resetSim(int firstDay);

	void shortDay(boolean value);

	void setRange(MotorId id, int max);
}
