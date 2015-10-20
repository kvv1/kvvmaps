package kvv.heliostat.server.sensor;

import kvv.heliostatengine.ISensor;
import kvv.heliostatengine.SensorState;

public interface Sensor extends ISensor {

	void close();

	void start();
}
