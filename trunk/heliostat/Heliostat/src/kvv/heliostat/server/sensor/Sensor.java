package kvv.heliostat.server.sensor;

import kvv.heliostat.shared.SensorState;

public interface Sensor {
	SensorState getState();
	void close();
	void start();
}
