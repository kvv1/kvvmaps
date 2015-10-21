package kvv.heliostat.server;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.client.dto.Params;
import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.controller.Controller;
import kvv.heliostat.server.controller.adu.ADUTransceiver;
import kvv.heliostat.server.controller.adu.PacketTransceiver;
import kvv.heliostat.server.motor.MotorRawSim;
import kvv.heliostat.server.sensor.Sensor;
import kvv.heliostat.server.sensor.SensorImpl;
import kvv.heliostat.server.sensor.SensorSim;
import kvv.stdutils.Utils;

public class Heliostat extends Looper {

	public static final Heliostat instance = new Heliostat();

	private static final String PARAMS_PATH = "c:/heliostat/params.json";

	private final Controller controller = new Controller();

	private final MotorRawSim motorAzimuthRaw = new MotorRawSim();
	private final MotorRawSim motorAltitudeRaw = new MotorRawSim();

	private Motor azMotor = new Motor(motorAzimuthRaw);
	private Motor altMotor = new Motor(motorAltitudeRaw);

	private final Motor[] motors = { azMotor, altMotor };
	private final Sensor sensor = new SensorSim(motorAzimuthRaw,
			motorAltitudeRaw);
	private final Sensor sensor1 = new SensorImpl(controller);

	public Params params = new Params();
	public Properties controllerParams = new Properties();

	public HeliostatState heliostatState;

	private final Engine engine = new Engine(sensor, motors);

	public synchronized HeliostatState getState() {
		return heliostatState;
	}

	public Heliostat() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				synchronized (Heliostat.this) {
					if (params.clock)
						Time.step(params.stepMS, params.shortDay);

					motors[0].simStep(params.stepMS);
					motors[1].simStep(params.stepMS);

					engine.step(params.auto, params.stepsPerDegree[0],
							params.stepsPerDegree[1]);

					SensorState sensorState = sensor.getState();
					MotorState[] motorStates = new MotorState[] {
							motors[0].getState(), motors[1].getState() };

					heliostatState = new HeliostatState(motorStates,
							sensorState, params, Time.getDay(), Time.getDayS(),
							Time.getTime(), Time.getTimeS(),
							engine.getAzData(), engine.getAltData());

					post(this, params.stepMS / params.clockRate);
				}
			}
		};
		post(r, 100);
	}

	public static void stopThread() {
		instance.close();
	}

	public static void startThread() {
		instance.start();
	}

	public synchronized void start() {

		new File(PARAMS_PATH).getParentFile().mkdirs();
		try {
			params = Utils.jsonRead(PARAMS_PATH, Params.class);
			controllerParamsChanged();
		} catch (IOException e) {
		}

		super.start();
		sensor.start();
	}

	private void controllerParamsChanged() throws IOException {
		controllerParams.load(new StringReader(params.controllerParams));
		String com = Heliostat.instance.controllerParams.getProperty("COM", "");
		controller.setModbusLine(new ADUTransceiver(new PacketTransceiver(com,
				500)));
	}

	private void writeParams() {
		try {
			Utils.jsonWrite(PARAMS_PATH, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void close() {
		stop();
		sensor.close();
		controller.close();
	}

	private void scheduleStep(long delay) {
		// post(new Runnable() {
		// @Override
		// public void run() {
		// step();
		// }
		// }, delay);
	}

	public synchronized void setAuto(AutoMode auto) {
		scheduleStep(0);
		params.auto = auto;
		writeParams();
		motors[0].stop();
		motors[1].stop();
	}

	public synchronized void move(MotorId id, int pos) {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		motors[id.ordinal()].go(pos);
	}

	public synchronized void stop(MotorId id) {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		motors[id.ordinal()].stop();
	}

	public synchronized void home(MotorId id) {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		motors[id.ordinal()].goHome();
	}

	public synchronized void moveRaw(MotorId id, int steps) {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		motors[id.ordinal()].moveRaw(steps);
	}

	public synchronized void setClock(boolean value) {
		params.clock = value;
		writeParams();
	}

	public synchronized void setStepsPerDegree(MotorId id, int value) {
		params.stepsPerDegree[id.ordinal()] = value;
		writeParams();
	}

	public synchronized void setClockRate(int value) {
		params.clockRate = value;
		writeParams();
	}

	public synchronized void setStepMS(int value) {
		params.stepMS = value;
		writeParams();
	}

	public synchronized void setDay(int day) {
		Time.setDay(day);
	}

	public synchronized void setTime(double time) {
		Time.setTime(time);
	}

	public synchronized void clearHistory() {
		engine.clearHistory();
	}

	public synchronized void shortDay(boolean value) {
		params.shortDay = value;
		writeParams();
	}

	public synchronized void setRange(MotorId id, int max) {
		params.range[id.ordinal()] = max;
		writeParams();
	}

	public synchronized void setControllerParams(String str) {
		params.controllerParams = str;
		writeParams();
		try {
			controllerParamsChanged();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
