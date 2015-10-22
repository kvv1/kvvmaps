package kvv.heliostat.server.envir.sensor;

import kvv.heliostat.client.dto.DayTime;
import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.server.ParamsHolder;
import kvv.heliostat.server.envir.Envir;
import kvv.heliostat.server.envir.motor.MotorRawSim;
import kvv.heliostat.shared.math.MirrorAngles;
import kvv.simpleutils.spline.Function;
import kvv.simpleutils.spline.FunctionFactory;
import kvv.simpleutils.spline.SplineInterpolator;

public class SensorSim implements Sensor {

	private static final int SENSOR_SENS_WIDTH = 6;
	private static final double SENSOR_SEGMENT_SENTER_DIST = 1;

	private final MotorRawSim azMotor;
	private final MotorRawSim altMotor;

	public SensorSim(MotorRawSim azMotor, MotorRawSim altMotor) {
		this.altMotor = altMotor;
		this.azMotor = azMotor;
	}

	@Override
	public SensorState getState() {

		Function azDeg2Steps = FunctionFactory.getFunction(
				ParamsHolder.params.simParams.azDeg2Steps[0],
				ParamsHolder.params.simParams.azDeg2Steps[1]);
		Function altDeg2Steps = FunctionFactory.getFunction(
				ParamsHolder.params.simParams.altDeg2Steps[0],
				ParamsHolder.params.simParams.altDeg2Steps[1]);

		double motorAz = FunctionFactory.solve(azDeg2Steps, azMotor.posAbs,
				ParamsHolder.params.simParams.MIN_AZIMUTH,
				ParamsHolder.params.simParams.MAX_AZIMUTH, 0.01);
		double motorAlt = FunctionFactory.solve(altDeg2Steps, altMotor.posAbs,
				ParamsHolder.params.simParams.MIN_ALTITUDE,
				ParamsHolder.params.simParams.MAX_ALTITUDE, 0.01);

		DayTime time = Envir.instance.time.getTime();

		double sunAz = MirrorAngles.get(time.day, time.time).x;
		double sunAlt = MirrorAngles.get(time.day, time.time).y;

		double dAz = motorAz - sunAz/* + (Math.random() - 0.5) */;
		double dAlt = motorAlt - sunAlt/* + (Math.random() - 0.5) */;

		double brightness = 1000;// * Environment.getMirrorAltitude(day, time) /
									// 45;
		if (!Envir.instance.weather.isSunny(time.day, time.time))
			brightness /= 50;

		double tl = sensorSensitivity.value(dist(-SENSOR_SEGMENT_SENTER_DIST,
				SENSOR_SEGMENT_SENTER_DIST, dAz, dAlt)) * brightness;
		double tr = sensorSensitivity.value(dist(SENSOR_SEGMENT_SENTER_DIST,
				SENSOR_SEGMENT_SENTER_DIST, dAz, dAlt)) * brightness;
		double bl = sensorSensitivity.value(dist(-SENSOR_SEGMENT_SENTER_DIST,
				-SENSOR_SEGMENT_SENTER_DIST, dAz, dAlt)) * brightness;
		double br = sensorSensitivity.value(dist(SENSOR_SEGMENT_SENTER_DIST,
				-SENSOR_SEGMENT_SENTER_DIST, dAz, dAlt)) * brightness;

		return new SensorState((int) tl, (int) tr, (int) bl, (int) br);
	}

	private static final double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	private static Function sensorSensitivity = new Function() {
		Function f = new SplineInterpolator().interpolate(new double[] {
				-SENSOR_SENS_WIDTH, -SENSOR_SENS_WIDTH / 2, 0,
				SENSOR_SENS_WIDTH / 2, SENSOR_SENS_WIDTH }, new double[] { 0,
				0.3, 1, 0.3, 0 });

		@Override
		public double value(double v) {
			if (Math.abs(v) < SENSOR_SENS_WIDTH)
				return f.value(v);
			return 0;
		}
	};

}
