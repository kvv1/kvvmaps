package kvv.heliostat.server;

import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.engine.Engine;
import kvv.heliostat.server.envir.Envir;

public class Heliostat extends Looper {

	public static final Heliostat instance = new Heliostat();

	private Engine engine = new Engine();

	public synchronized void close() {
		stop();
	}

	public synchronized void init() {
		start();

		post(new Runnable() {
			private long t = System.currentTimeMillis();

			@Override
			public void run() {
				long t1 = System.currentTimeMillis();
				int dt = (int) (t1 - t);
				if (ParamsHolder.params.SIM)
					dt *= ParamsHolder.params.simParams.clockRate;
				t = t1;
				Envir.instance.step(dt);
				step();
				post(this, 10);
			}
		}, 2000);

		post(new Runnable() {
			@Override
			public void run() {
				if (ParamsHolder.params.clock
						&& ParamsHolder.params.auto != AutoMode.OFF) {
					engine.engineStep();
				}

				int stepMS = ParamsHolder.params.stepMS;
				if (ParamsHolder.params.SIM)
					stepMS /= ParamsHolder.params.simParams.clockRate;

				post(this, stepMS);
			}
		}, 2000);
	}

	private void step() {
		Envir envir = Envir.instance;

		envir.sensor.updateState();
		envir.motors[0].updateState();
		envir.motors[1].updateState();

	}

	public HeliostatState getState() {
		Envir envir = Envir.instance;

		SensorState sensorState = envir.sensor.getState();
		MotorState[] motorStates = new MotorState[] {
				envir.motors[0].getState(), envir.motors[1].getState() };

		return new HeliostatState(motorStates, sensorState,
				ParamsHolder.params, envir.time.getTime(), engine.getAzData(),
				engine.getAltData());
	}

	public void clearHistory() {
		engine.clearHistory();
	}

}
