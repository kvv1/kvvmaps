package kvv.heliostat.server.envir.motor;

import kvv.heliostat.client.dto.MotorState;

public interface Motor {

	void close();

	void goHome();

	void go(int pos);

	void moveRaw(int steps);

	void stop();

	MotorState getState();

	MotorState updateState();

	void simStep(int ms);
}
