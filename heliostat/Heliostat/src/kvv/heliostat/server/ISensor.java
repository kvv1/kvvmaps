package kvv.heliostat.server;

import kvv.heliostat.client.dto.SensorState;

public interface ISensor {
	SensorState getState();
}
