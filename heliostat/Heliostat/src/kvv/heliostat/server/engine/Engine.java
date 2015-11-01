package kvv.heliostat.server.engine;

import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.DayTime;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.ParamsHolder;
import kvv.heliostat.server.envir.Envir;
import kvv.heliostat.shared.math.MirrorAngles;
import kvv.simpleutils.src.PtD;

public class Engine {

	private AngleStepTable azTable = new AngleStepTable(-60, 60,
			ParamsHolder.params.ANGLE_STEP, "c:/heliostat/motorAz.txt");
	private AngleStepTable altTable = new AngleStepTable(-10, 60,
			ParamsHolder.params.ANGLE_STEP, "c:/heliostat/motorAlt.txt");

	public void engineStep() {
		SensorState sensorState = Envir.instance.sensor.getState();
		MotorState azMotorState = Envir.instance.motors[0].getState();
		MotorState altMotorState = Envir.instance.motors[1].getState();

		if (!azMotorState.posValid || !altMotorState.posValid)
			return;

		DayTime time = Envir.instance.time.getTime();

		if (sensorState.isValid()) {
			double deflX = sensorState.getDeflectionX();
			double deflY = sensorState.getDeflectionY();

			int azPos = (int) (0.5 + (azMotorState.pos - deflX
					* ParamsHolder.params.stepsPerDegree[0]));
			int altPos = (int) (0.5 + (altMotorState.pos - deflY
					* ParamsHolder.params.stepsPerDegree[1]));

			if (Math.sqrt(deflX * deflX + deflY * deflY) < 0.02) {
				azTable.add(MirrorAngles.get(time.day, time.time).x, azPos);
				altTable.add(MirrorAngles.get(time.day, time.time).y, altPos);
			}

			Envir.instance.motors[0].go(azPos);
			Envir.instance.motors[1].go(altPos);
		} else if (ParamsHolder.params.auto == AutoMode.FULL) {
			PtD angles = MirrorAngles.get(time.day, time.time);
			int azPos = (int) azTable.get(angles.x);
			int altPos = (int) altTable.get(angles.y);
			if (azPos != Double.NaN && altPos != Double.NaN) {
				Envir.instance.motors[0].go(azPos);
				Envir.instance.motors[1].go(altPos);
			}
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
