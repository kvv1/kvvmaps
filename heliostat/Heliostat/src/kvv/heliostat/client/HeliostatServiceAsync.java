package kvv.heliostat.client;

import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HeliostatServiceAsync {

	void getState(int reqId, AsyncCallback<HeliostatState> callback);

	void setAuto(AutoMode auto, AsyncCallback<Void> callback);

	void clock(boolean value, AsyncCallback<Void> callback);

	void move(MotorId id, int pos, AsyncCallback<Void> callback);

	void stop(MotorId id, AsyncCallback<Void> callback);

	void home(MotorId id, AsyncCallback<Void> callback);

	void moveRaw(MotorId id, int steps, AsyncCallback<Void> callback);

	void setStepsPerDegree(MotorId id, int value, AsyncCallback<Void> callback);

	void setAlgorithmStepMS(int value, AsyncCallback<Void> callback);

	void clearHistory(AsyncCallback<Void> callback);

	void setControllerParams(String str, AsyncCallback<Void> callback);

	void setRange(MotorId id, int max, AsyncCallback<Void> callback);
}
