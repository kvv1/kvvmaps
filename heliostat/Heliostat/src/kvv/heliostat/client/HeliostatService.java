package kvv.heliostat.client;

import kvv.gwtutils.client.login.AuthException;
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

	void move(MotorId id, int pos) throws AuthException;

	void stop(MotorId id) throws AuthException;

	void home(MotorId id) throws AuthException;

	void moveRaw(MotorId id, int steps) throws AuthException;

	void setAuto(AutoMode auto) throws AuthException;

	void home() throws AuthException;

	void setTime(double time) throws AuthException;

	void clock(boolean value) throws AuthException;

	void setStepsPerDegree(MotorId id, int value) throws AuthException;

	void setClockRate(int value) throws AuthException;

	void setStepMS(int value) throws AuthException;

	void setDay(int day) throws AuthException;

	void clearHistory() throws AuthException;

	Weather getWeather();

	void saveWeather(Weather weather) throws AuthException;

	Weather resetSim(int firstDay) throws AuthException;

	void shortDay(boolean value) throws AuthException;

	void setRange(MotorId id, int max) throws AuthException;

	void setControllerParams(String str) throws AuthException;
}
