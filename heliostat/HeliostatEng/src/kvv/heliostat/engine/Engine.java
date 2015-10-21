package kvv.heliostat.engine;

import java.sql.Time;

import kvv.heliostat.shared.AutoMode;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorState;
import kvv.heliostat.shared.SensorState;

import org.omg.CORBA.Environment;

public abstract class Engine {
	public HeliostatState heliostatState;

	private final Motor[] motors;
	private final Sensor sensor;

	public Engine(Sensor sensor, Motor[] motors) {
		this.sensor = sensor;
		this.motors = motors;
	}

	protected abstract Params getParams();

	private AngleStepTable azTable = new AngleStepTable(-60, 60,
			Environment.ANGLE_STEP, "c:/heliostat/motorAz.txt");
	private AngleStepTable altTable = new AngleStepTable(-10, 60,
			Environment.ANGLE_STEP, "c:/heliostat/motorAlt.txt");

	public void step() {
		Params params = getParams();

		SensorState sensorState = sensor.getState();

		MotorState[] motorStates = new MotorState[] { motors[0].getState(),
				motors[1].getState() };

		heliostatState = new HeliostatState(motorStates, sensorState, params,
				Time.getDay(), Time.getDayS(), Time.getTime(), Time.getTimeS(),
				azTable.getData(), altTable.getData());

		if (!motorStates[0].posValid || !motorStates[1].posValid)
			return;

		if (params.auto == AutoMode.OFF)
			return;

		if (sensorState.isValid()) {
			double deflX = sensorState.getDeflectionX();
			double deflY = sensorState.getDeflectionY();

			int azPos = (int) (0.5 + (motorStates[0].pos - deflX
					* params.stepsPerDegree[0]));
			int altPos = (int) (0.5 + (motorStates[1].pos - deflY
					* params.stepsPerDegree[1]));

			if (Math.sqrt(deflX * deflX + deflY * deflY) < 1) {
				azTable.add(MirrorAngles.get(Time.getDay(), Time.getTime()).x,
						azPos);
				altTable.add(MirrorAngles.get(Time.getDay(), Time.getTime()).y,
						altPos);
			}

			motors[0].go(azPos);
			motors[1].go(altPos);
		} else if (params.auto == AutoMode.FULL) {
			PtD angles = MirrorAngles.get(Time.getDay(), Time.getTime()
					+ params.stepMS / (3600000d));
			int azPos = (int) azTable.get(angles.x);
			int altPos = (int) altTable.get(angles.y);
			motors[0].go(azPos);
			motors[1].go(altPos);
		}

	}

	public void clearHistory() {
		azTable.clear();
		altTable.clear();
	}

}
