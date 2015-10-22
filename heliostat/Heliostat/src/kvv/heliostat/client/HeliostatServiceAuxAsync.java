package kvv.heliostat.client;

import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.dto.Weather;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HeliostatServiceAuxAsync {

	void setClockRate(int value, AsyncCallback<Void> callback);

	void getWeather(AsyncCallback<Weather> callback);

	void saveWeather(Weather weather, AsyncCallback<Void> callback);

	void resetSim(int firstDay, AsyncCallback<Weather> callback);

	void shortDay(boolean value, AsyncCallback<Void> callback);

	void setRange(MotorId id, int max, AsyncCallback<Void> callback);

	void setDay(int day, AsyncCallback<Void> callback);

	void setTime(double time, AsyncCallback<Void> callback);

}
