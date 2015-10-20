package kvv.heliostat.server.sensor;

import kvv.heliostat.engine.ISensor;
import kvv.heliostat.engine.SensorState;

public interface Sensor extends ISensor {

	void close();

	void start();
}
