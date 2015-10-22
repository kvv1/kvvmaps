package kvv.heliostat.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.gwtutils.client.login.AuthException;
import kvv.gwtutils.server.login.LoginServlet;
import kvv.heliostat.client.HeliostatService;
import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.server.envir.Envir;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class HeliostatServiceImpl extends LoginServlet implements
		HeliostatService {

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		synchronized (Heliostat.instance) {
			super.service(arg0, arg1);
		}
	}

	@Override
	public HeliostatState getState() {
		return Heliostat.instance.heliostatState;
	}

	@Override
	public void setAuto(AutoMode auto) throws AuthException {
		checkUser();
		ParamsHolder.params.auto = auto;
		ParamsHolder.writeParams();
		Envir.instance.motors[0].stop();
		Envir.instance.motors[1].stop();
	}

	@Override
	public void clock(boolean value) throws AuthException {
		checkUser();
		ParamsHolder.params.clock = value;
		ParamsHolder.writeParams();
	}

	@Override
	public void move(MotorId id, int pos) throws AuthException {
		checkUser();
		ParamsHolder.params.auto = AutoMode.OFF;
		ParamsHolder.writeParams();
		Envir.instance.motors[id.ordinal()].go(pos);
	}

	@Override
	public void stop(MotorId id) throws AuthException {
		checkUser();
		ParamsHolder.params.auto = AutoMode.OFF;
		ParamsHolder.writeParams();
		Envir.instance.motors[id.ordinal()].stop();
	}

	@Override
	public void home(MotorId id) throws AuthException {
		checkUser();
		ParamsHolder.params.auto = AutoMode.OFF;
		ParamsHolder.writeParams();
		Envir.instance.motors[id.ordinal()].goHome();
	}

	@Override
	public void moveRaw(MotorId id, int steps) throws AuthException {
		checkUser();
		ParamsHolder.params.auto = AutoMode.OFF;
		ParamsHolder.writeParams();
		Envir.instance.motors[id.ordinal()].moveRaw(steps);
	}

	@Override
	public void setStepsPerDegree(MotorId id, int value) throws AuthException {
		checkUser();
		ParamsHolder.params.stepsPerDegree[id.ordinal()] = value;
		ParamsHolder.writeParams();
	}

	@Override
	public void setAlgorithmStepMS(int value) throws AuthException {
		checkUser();
		ParamsHolder.params.stepMS = value;
		ParamsHolder.writeParams();
	}

	@Override
	public void clearHistory() throws AuthException {
		checkUser();
		Heliostat.instance.clearHistory();
	}

	@Override
	public void setControllerParams(String str) throws AuthException {
		checkUser();
		ParamsHolder.params.controllerParams = str;
		ParamsHolder.writeParams();
	}

}
