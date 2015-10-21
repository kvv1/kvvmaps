package kvv.heliostat.client;

import kvv.gwtutils.client.login.AuthException;
import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("heliostat")
public interface HeliostatService extends RemoteService {
	HeliostatState getState();

	void move(MotorId id, int pos) throws AuthException;

	void stop(MotorId id) throws AuthException;

	void home(MotorId id) throws AuthException;

	void moveRaw(MotorId id, int steps) throws AuthException;

	void setAuto(AutoMode auto) throws AuthException;

	void setDay(int day) throws AuthException;

	void setTime(double time) throws AuthException;

	void clock(boolean value) throws AuthException;

	void setStepsPerDegree(MotorId id, int value) throws AuthException;

	void setAlgorithmStepMS(int value) throws AuthException;

	void clearHistory() throws AuthException;

	void setControllerParams(String str) throws AuthException;
}
