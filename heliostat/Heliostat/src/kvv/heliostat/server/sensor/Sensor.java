package kvv.heliostat.server.sensor;

import kvv.heliostat.client.dto.SensorState;

public interface Sensor {
	SensorState getState();
}
