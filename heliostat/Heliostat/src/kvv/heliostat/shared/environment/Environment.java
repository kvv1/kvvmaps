package kvv.heliostat.shared.environment;

import kvv.heliostat.shared.math.MirrorAngles;
import kvv.heliostat.shared.spline.Function;
import kvv.heliostat.shared.spline.FunctionFactory;

public class Environment {

	public static double getMirrorAzimuth(int dateOfYear, double time) {
		double az = MirrorAngles.get(dateOfYear, time).x;
		return az;
	}

	public static double getMirrorAltitude(int dateOfYear, double time) {
		double alt = MirrorAngles.get(dateOfYear, time).y;
		return alt;
	}

	public static final int MIN_AZIMUTH = -60;
	public static final int MAX_AZIMUTH = 60;
	public static final int MIN_ALTITUDE = -10;
	public static final int MAX_ALTITUDE = 55;

	
	public final static int MAX_STEPS = 80000;

	public static Function azDeg2Steps = FunctionFactory.getFunction(
			new double[] { MIN_AZIMUTH, 20, MAX_AZIMUTH }, new double[] { 0,
					MAX_STEPS / 2, MAX_STEPS });

	public static Function altDeg2Steps = FunctionFactory.getFunction(
			new double[] { MIN_ALTITUDE, 30, MAX_ALTITUDE }, new double[] { 0,
					MAX_STEPS / 2, MAX_STEPS });

}
