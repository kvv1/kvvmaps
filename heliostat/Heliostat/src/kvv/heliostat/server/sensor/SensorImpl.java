package kvv.heliostat.server.sensor;

import kvv.heliostat.server.Heliostat;
import kvv.heliostat.server.controller.Controller;
import kvv.heliostatengine.SensorState;

public class SensorImpl implements Sensor {

	private final Controller controller;
	private volatile boolean stopped;

	private final SensorState state0 = new SensorState(0, 0, 0, 0);
	private volatile SensorState state = state0;

	public SensorImpl(Controller controller) {
		this.controller = controller;
	}

	@Override
	public SensorState getState() {
		return state;
	}

	private Thread thread = new Thread() {
		public void run() {
			while (!stopped) {
				try {
					sleep(100);
					int ADDR = Integer
							.parseInt(Heliostat.instance.controllerParams
									.getProperty("SENSOR_ADDR", "0"));
					
					controller.setReg(ADDR, 10, 0);
					
					int[] resp = controller.getRegs(ADDR, 16, 4);
					state = new SensorState(resp[0], resp[1], resp[2], resp[3]);
				} catch (Exception e) {
					state = state0;
				}
			}
		}
	};

	@Override
	public void close() {
		stopped = true;
	}

	@Override
	public void start() {
		thread.start();
	}

}
