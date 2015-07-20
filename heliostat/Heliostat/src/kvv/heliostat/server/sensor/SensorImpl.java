package kvv.heliostat.server.sensor;

import kvv.controllers.controller.Controller;
import kvv.heliostat.shared.SensorState;

public class SensorImpl implements Sensor {

	private static final int ADDR = 25;

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
