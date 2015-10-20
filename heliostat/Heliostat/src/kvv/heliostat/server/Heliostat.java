package kvv.heliostat.server;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import kvv.heliostat.server.controller.Controller;
import kvv.heliostat.server.controller.adu.ADUTransceiver;
import kvv.heliostat.server.controller.adu.PacketTransceiver;
import kvv.heliostat.server.motor.Motor;
import kvv.heliostat.server.motor.MotorRawSim;
import kvv.heliostat.server.sensor.Sensor;
import kvv.heliostat.server.sensor.SensorImpl;
import kvv.heliostat.server.sensor.SensorSim;
import kvv.heliostat.server.trajectory.TrajectoryImpl;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorId;
import kvv.heliostat.shared.MotorState;
import kvv.heliostat.shared.Params;
import kvv.heliostat.shared.Params.AutoMode;
import kvv.heliostat.shared.SensorState;
import kvv.heliostat.shared.Weather;
import kvv.simpleutils.src.PtI;
import kvv.stdutils.Utils;

public class Heliostat extends Looper {

	public static final Heliostat instance = new Heliostat();

	private static final String PARAMS_PATH = "c:/heliostat/params.json";
	private static final String CONTROLLER_PARAMS_PATH = "c:/heliostat/controllers.txt";

	private final Controller controller = new Controller();

	private final MotorRawSim motorAzimuthRaw = new MotorRawSim();
	private final MotorRawSim motorAltitudeRaw = new MotorRawSim();

	private Motor azMotor = new Motor(motorAzimuthRaw);
	private Motor altMotor = new Motor(motorAltitudeRaw);

	private final Motor[] motors = { azMotor, altMotor };
	private final Sensor sensor1 = new SensorSim(motorAzimuthRaw,
			motorAltitudeRaw);
	private final Sensor sensor = new SensorImpl(controller);

	public Params params = new Params();
	public Properties controllerParams = new Properties();
	public String controllerParamsText = "";

	private final TrajectoryImpl trajectory = new TrajectoryImpl();

	public synchronized HeliostatState getState() {
		return heliostatState;
	}

	private HeliostatState heliostatState;

	public SimEnvironment simEnvironment = new SimEnvironment();

	public Heliostat() {

		Runnable r = new Runnable() {
			long t;

			@Override
			public void run() {
				t = System.currentTimeMillis();
				step();
				post(this, params.stepMS / params.clockRate);
				// post(this, t + params.stepMS / params.clockRate -
				// System.currentTimeMillis());
			}
		};

		post(r, 100);
	}

	private void step() {
		if (params.clock)
			Time.step(params.stepMS, params.shortDay);

		motors[0].simStep(params.stepMS);
		motors[1].simStep(params.stepMS);

		MotorState[] motorStates = new MotorState[] {
				motors[0].getState(), motors[1].getState() };
		
		heliostatState = new HeliostatState(motorStates,
				sensor.getState(), params, controllerParamsText, Time.getDay(),
				Time.getDayS(), Time.getTime(), Time.getTimeS(),
				trajectory.getAzData(), trajectory.getAltData(), isSunny());

		if (heliostatState.motorState[0].posValid
				&& heliostatState.motorState[1].posValid) {
			SensorState ss = heliostatState.sensorState;

			PtI motorsPositions = trajectory.getMotorsPositions(params.auto,
					(ss != null && ss.valueValid) ? ss.deflection : null,
					new PtI(heliostatState.motorState[0].pos,
							heliostatState.motorState[1].pos),
					params.stepMS / (3600000d));

			if (motorsPositions != null) {
				motors[0].go(motorsPositions.x);
				motors[1].go(motorsPositions.y);
			}
		}
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
			controllerParamsText = Utils.readFile(CONTROLLER_PARAMS_PATH);
			controllerParamsChanged();
		} catch (IOException e) {
		}

		super.start();
		sensor.start();
	}

	private void controllerParamsChanged() throws IOException {
		controllerParams.load(new StringReader(controllerParamsText));
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

	public boolean isSunny() {
		return simEnvironment.isSunny();
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

	public synchronized void home() {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		motors[0].goHome();
		motors[1].goHome();
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
		trajectory.clearHistory();
	}

	public synchronized Weather getWeather() {
		return simEnvironment.weather;
	}

	public synchronized void saveWeather(Weather weather) {
		try {
			simEnvironment.set(weather);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public synchronized Weather resetSim(int firstDay) {
		simEnvironment.reset(firstDay);
		return simEnvironment.weather;
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
		controllerParamsText = str;
		try {
			Utils.writeFile(CONTROLLER_PARAMS_PATH, controllerParamsText);
			controllerParamsChanged();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
