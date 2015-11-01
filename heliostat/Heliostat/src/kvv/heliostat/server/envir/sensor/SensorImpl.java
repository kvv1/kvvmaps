package kvv.heliostat.server.envir.sensor;

import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.ParamsHolder;
import kvv.heliostat.server.envir.controller.Controller;

public class SensorImpl implements Sensor {

	private final Controller controller;

	private final SensorState state0 = new SensorState(0, 0, 0, 0, 0);
	private volatile SensorState state = state0;
	private int t = 0;
	private int tCnt;

	public SensorImpl(Controller controller) {
		this.controller = controller;
	}

	@Override
	public SensorState getState() {
		return state;
	}

	private SensorState _getState() {
		try {
			int ADDR = Integer.parseInt(ParamsHolder.controllerParams
					.getProperty("SENSOR_ADDR", "0"));

			tCnt--;
			if (tCnt < 0) {
				tCnt = 10;
				controller.setReg(ADDR, 10, 0);
				t = controller.getReg(ADDR, 9);
			}

			int[] resp = controller.getRegs(ADDR, 16, 4);
			return new SensorState(resp[0], resp[1], resp[2], resp[3], t);
		} catch (Exception e) {
			return state0;
		}
	}

	public void close() {
	}

	public void start() {
	}

	@Override
	public SensorState updateState() {
		state = _getState();
		return state;
	}

}
