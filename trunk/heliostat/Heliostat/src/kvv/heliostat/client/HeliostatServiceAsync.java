package kvv.heliostat.client;

import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorId;
import kvv.heliostat.shared.Params.AutoMode;
import kvv.heliostat.shared.Weather;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface HeliostatServiceAsync {
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void getState(AsyncCallback<HeliostatState> callback);

	void setAuto(AutoMode auto, AsyncCallback<Void> callback);

	void home(AsyncCallback<Void> callback);

	void setTime(double time, AsyncCallback<Void> callback);

	void clock(boolean value, AsyncCallback<Void> callback);

	void move(MotorId id, int pos, AsyncCallback<Void> callback);

	void stop(MotorId id, AsyncCallback<Void> callback);

	void home(MotorId id, AsyncCallback<Void> callback);

	void moveRaw(MotorId id, int steps, AsyncCallback<Void> callback);

	void calibrate(MotorId id, AsyncCallback<Void> callback);

	void setStepsPerDegree(MotorId id, int value, AsyncCallback<Void> callback);

	void setClockRate(int value, AsyncCallback<Void> callback);

	void setStepMS(int value, AsyncCallback<Void> callback);

	void setDay(int day, AsyncCallback<Void> callback);

	void clearHistory(AsyncCallback<Void> callback);

	void saveWeather(Weather weather, AsyncCallback<Void> callback);

	void getWeather(AsyncCallback<Weather> callback);

	void resetSim(int firstDay, AsyncCallback<Weather> callback);

	void shortDay(boolean value, AsyncCallback<Void> callback);
}
