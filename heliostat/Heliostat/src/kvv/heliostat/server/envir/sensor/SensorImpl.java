package kvv.heliostat.server.envir.sensor;

import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.envir.Envir;
import kvv.heliostat.server.envir.RealEnvir;
import kvv.heliostat.server.envir.controller.Controller;

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
					int ADDR = Integer.parseInt(Envir.instance.getProps()
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

	public void close() {
		stopped = true;
	}

	public void start() {
		thread.start();
	}

}