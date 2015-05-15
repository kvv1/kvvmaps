package kvv.heliostat.server.sensor;

import java.io.IOException;

import kvv.heliostat.server.controller.Controller;
import kvv.heliostat.shared.PtD;
import kvv.heliostat.shared.SensorState;

public class SensorImpl implements Sensor {

	private final Controller controller;

	public SensorImpl(Controller controller) {
		this.controller = controller;
	}

	@Override
	public SensorState getState() {
		try {
			int[] resp = controller.getRegs(26, 16, 4);

			return new SensorState(false, new PtD( 0, 0 ), resp[0],
					resp[1], resp[2], resp[3]);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

}
