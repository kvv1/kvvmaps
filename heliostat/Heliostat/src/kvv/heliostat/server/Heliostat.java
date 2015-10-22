package kvv.heliostat.server;

import kvv.heliostat.client.dto.DayTime;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.engine.Engine;
import kvv.heliostat.server.envir.Envir;

public class Heliostat extends Looper {

	public static final Heliostat instance = new Heliostat();

	public HeliostatState heliostatState;

	private final Engine engine = new Engine(Envir.instance.sensor,
			Envir.instance.motors);

	Runnable r = new Runnable() {
		@Override
		public void run() {
			synchronized (Heliostat.this) {
				if (ParamsHolder.params.clock) {

					Envir envir = Envir.instance;

					DayTime time = envir.time.getTime();

					envir.step(ParamsHolder.params.stepMS);

					engine.step(ParamsHolder.params.auto,
							ParamsHolder.params.stepsPerDegree[0],
							ParamsHolder.params.stepsPerDegree[1], time.day,
							time.time);

					SensorState sensorState = envir.sensor.getState();
					MotorState[] motorStates = new MotorState[] {
							envir.motors[0].getState(),
							envir.motors[1].getState() };

					heliostatState = new HeliostatState(motorStates,
							sensorState, ParamsHolder.params, time,
							engine.getAzData(), engine.getAltData());
				}

				post(this, ParamsHolder.params.stepMS
						/ ParamsHolder.params.simParams.clockRate);
			}
		}
	};

	public synchronized void init() {
		start();
		Envir.instance.start();
		post(r, 100);
	}

	public synchronized void close() {
		stop();
		Envir.instance.close();
	}

	public void clearHistory() {
		engine.clearHistory();
	}

}
