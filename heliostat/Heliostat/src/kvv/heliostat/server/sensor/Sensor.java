package kvv.heliostat.server.sensor;

import kvv.heliostat.shared.SensorState;

public interface Sensor extends ISensor {

	void close();

	void start();
}
