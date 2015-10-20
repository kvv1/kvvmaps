package kvv.heliostat.server.sensor;

import kvv.heliostat.shared.SensorState;

public interface ISensor {
	SensorState getState();
}
