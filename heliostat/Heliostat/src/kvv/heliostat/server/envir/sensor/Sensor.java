package kvv.heliostat.server.envir.sensor;

import kvv.heliostat.client.dto.SensorState;

public interface Sensor {
	SensorState getState();
	SensorState updateState();
}
