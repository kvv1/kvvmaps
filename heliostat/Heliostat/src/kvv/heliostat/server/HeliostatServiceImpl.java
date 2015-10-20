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
	public void setAlgorithmStepMS(int value)  throws AuthException {
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
	public void setControllerParams(String str) throws AuthException {
		checkUser();
		Heliostat.instance.setControllerParams(str);
	}

}
