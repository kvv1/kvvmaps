package kvv.heliostat.server;

import java.io.File;
import java.io.IOException;

import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.DayTime;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.client.dto.Params;
import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.engine.Engine;
import kvv.heliostat.server.envir.Envir;
import kvv.stdutils.Utils;

public class Heliostat extends Looper {

	public static final Heliostat instance = new Heliostat();

	private static final String PARAMS_PATH = "c:/heliostat/params.json";

	public Params params = new Params();

	public HeliostatState heliostatState;

	private final Engine engine = new Engine(Envir.instance.sensor,
			Envir.instance.motors);

	public synchronized HeliostatState getState() {
		return heliostatState;
	}

	public Heliostat() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				synchronized (Heliostat.this) {
					Envir envir = Envir.instance;

					DayTime time = envir.time.getTime();

					envir.step(params.stepMS);

					engine.step(params.auto, params.stepsPerDegree[0],
							params.stepsPerDegree[1], time.day, time.time);

					SensorState sensorState = envir.sensor.getState();
					MotorState[] motorStates = new MotorState[] {
							envir.motors[0].getState(),
							envir.motors[1].getState() };

					heliostatState = new HeliostatState(motorStates,
							sensorState, params, time, engine.getAzData(),
							engine.getAltData());

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
			Envir.instance.paramsChanged(params);
		} catch (IOException e) {
		}

		super.start();
		Envir.instance.start();
	}

	private void writeParams() {
		try {
			Utils.jsonWrite(PARAMS_PATH, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Envir.instance.paramsChanged(params);
	}

	private void close() {
		stop();
		Envir.instance.close();
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
		Envir.instance.motors[0].stop();
		Envir.instance.motors[1].stop();
	}

	public synchronized void move(MotorId id, int pos) {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		Envir.instance.motors[id.ordinal()].go(pos);
	}

	public synchronized void stop(MotorId id) {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		Envir.instance.motors[id.ordinal()].stop();
	}

	public synchronized void home(MotorId id) {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		Envir.instance.motors[id.ordinal()].goHome();
	}

	public synchronized void moveRaw(MotorId id, int steps) {
		scheduleStep(0);
		params.auto = AutoMode.OFF;
		writeParams();
		Envir.instance.motors[id.ordinal()].moveRaw(steps);
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
	}

}
