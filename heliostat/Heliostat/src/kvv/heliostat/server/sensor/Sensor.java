package kvv.heliostat.server.sensor;

import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.ISensor;

public interface Sensor extends ISensor {

	void close();

	void start();
}
