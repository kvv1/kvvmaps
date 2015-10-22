package kvv.heliostat.server;

import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.envir.Envir;
import kvv.heliostat.server.sensor.Sensor;
import kvv.heliostat.server.trajectory.AngleStepTable;
import kvv.heliostat.shared.environment.Environment;
import kvv.heliostat.shared.math.MirrorAngles;
import kvv.simpleutils.src.PtD;

public class Engine {

	private final Motor[] motors;
	private final Sensor sensor;

	public Engine(Sensor sensor, Motor[] motors) {
		this.sensor = sensor;
		this.motors = motors;
	}

	private AngleStepTable azTable = new AngleStepTable(-60, 60,
			Environment.ANGLE_STEP, "c:/heliostat/motorAz.txt");
	private AngleStepTable altTable = new AngleStepTable(-10, 60,
			Environment.ANGLE_STEP, "c:/heliostat/motorAlt.txt");

	public void step(AutoMode autoMode, int stepsPerDegreeAz,
			int stepsPerDegreeAlt, int day, double time) {
		SensorState sensorState = sensor.getState();

		MotorState azMotorState = motors[0].getState();
		MotorState altMotorState = motors[1].getState();

		if (!azMotorState.posValid || !altMotorState.posValid)
			return;

		if (autoMode == AutoMode.OFF)
			return;

		if (sensorState.isValid()) {
			double deflX = sensorState.getDeflectionX();
			double deflY = sensorState.getDeflectionY();

			int azPos = (int) (0.5 + (azMotorState.pos - deflX
					* stepsPerDegreeAz));
			int altPos = (int) (0.5 + (altMotorState.pos - deflY
					* stepsPerDegreeAlt));

			if (Math.sqrt(deflX * deflX + deflY * deflY) < 0.02) {
				azTable.add(MirrorAngles.get(day, time).x, azPos);
				altTable.add(MirrorAngles.get(day, time).y, altPos);
			}

			motors[0].go(azPos);
			motors[1].go(altPos);
		} else if (autoMode == AutoMode.FULL) {
			PtD angles = MirrorAngles.get(day, time);
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

	public double[][] getAzData() {
		return azTable.getData();
	}

	public double[][] getAltData() {
		return altTable.getData();
	}

}
