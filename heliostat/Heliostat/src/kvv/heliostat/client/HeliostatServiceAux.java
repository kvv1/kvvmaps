package kvv.heliostat.client;

import kvv.gwtutils.client.login.AuthException;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.dto.Weather;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("heliostatAux")
public interface HeliostatServiceAux extends RemoteService {
	void setClockRate(int value) throws AuthException;

	Weather getWeather();

	void saveWeather(Weather weather) throws AuthException;

	Weather resetSim(int firstDay) throws AuthException;

	void shortDay(boolean value) throws AuthException;

	void setRange(MotorId id, int max) throws AuthException;

}
