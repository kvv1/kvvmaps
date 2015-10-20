package kvv.heliostat.shared.math;

import kvv.simpleutils.spline.FunctionFactory;
import kvv.simpleutils.src.PtD;

public class MirrorAngles {

	private static final int FIRST_HOUR = 4;
	private static final int LAST_HOUR = 20;

	public static final double LAT = 12.0066228;
	public static final double LON = -79.8104811;
	public static final double TIMEZONE = -5.5;

	private static double[][][] mirrorTable = new double[366][LAST_HOUR
			- FIRST_HOUR + 1][2];

	static {
		for (int d = 0; d < mirrorTable.length; d++) {
			for (int h = FIRST_HOUR; h <= LAST_HOUR; h++) {
				double[] sun = calcSun(d, h, LAT, LON, TIMEZONE);
				sun[0] = -(180 - sun[0]);
				double[] mirror = calcMirror(sun);
				mirrorTable[d][h - FIRST_HOUR] = mirror;
			}
		}
	}

	public static PtD get(int day, double time) {
		double[][] dayTable = mirrorTable[day];

		int t = (int) time;

		double az;
		double alt;

		if (time < FIRST_HOUR + 1) {
			az = dayTable[1][0];
			alt = dayTable[1][1];
		} else if (time > LAST_HOUR - 1) {
			az = dayTable[LAST_HOUR - FIRST_HOUR - 1][0];
			alt = dayTable[LAST_HOUR - FIRST_HOUR - 1][1];
		} else {
			int idx = t - FIRST_HOUR;

			if (time == LAST_HOUR - 1) {
				idx--;
				t--;
			}

			az = FunctionFactory.q(time - t, dayTable[idx - 1][0],
					dayTable[idx][0], dayTable[idx + 1][0],
					dayTable[idx + 2][0]);

			alt = FunctionFactory.q(time - t, dayTable[idx - 1][1],
					dayTable[idx][1], dayTable[idx + 1][1],
					dayTable[idx + 2][1]);
		}

		return new PtD(az, alt);
	}

	public static double[] calcMirror(double[] arg) {
		double[] res = new double[2];

		double alpha = Math.toRadians(arg[0]);
		double beta = Math.toRadians(arg[1]);

		double x = Math.tan(alpha);
		double y = Math.sqrt(x * x + 1) * Math.tan(beta);

		double r = Math.sqrt(x * x + y * y);
		double gamma = Math.atan(r);

		double x1, y1;

		if (Math.cos(alpha) > 0) {
			double gamma1 = gamma / 2;
			double r1 = Math.tan(gamma1);
			x1 = r1 * x / r;
			y1 = r1 * y / r;
		} else {
			double gamma1 = (Math.PI - gamma) / 2;
			double r1 = Math.tan(gamma1);
			x1 = -r1 * x / r;
			y1 = r1 * y / r;
		}

		double alpha1 = Math.atan(x1);
		double beta1 = Math.atan(y1 / Math.sqrt(x1 * x1 + 1));

		res[0] = Math.toDegrees(alpha1);
		res[1] = Math.toDegrees(beta1);

		return res;
	}

	static double cosAz;
	static double ha;

	public static double[] calcSun(int dayOfYear, double hr, double lat,
			double lon, double timezone) {

		double lonDeg = lon;

		lat = Math.toRadians(lat);
		lon = Math.toRadians(lon);

		double g = 2 * Math.PI / 365 * (dayOfYear - 1 + (hr - 12) / 24);

		double eqtime = 229.18 * (0.000075 + 0.001868 * cos(g) - 0.032077
				* sin(g) - 0.014615 * cos(2 * g) - 0.040849 * sin(2 * g));

		double decl = 0.006918 - 0.399912 * cos(g) + 0.070257 * sin(g)
				- 0.006758 * cos(2 * g) + 0.000907 * sin(2 * g) - 0.002697
				* cos(3 * g) + 0.00148 * sin(3 * g);

		// System.out.println("decl = " + Math.toDegrees(decl));
		// System.out.println("eqtime = " + eqtime);

		double timeOffset = eqtime - 4 * lonDeg + 60 * timezone; // minutes

		double tst = hr * 60 + timeOffset;

		ha = (tst / 4) - 180;

		double cosPhi = sin(lat) * sin(decl) + cos(lat) * cos(decl)
				* cos(Math.toRadians(ha));
		// System.out.println("cosPhi = " + cosPhi);

		double phi = arccos(cosPhi);

		/* double */cosAz = -(sin(lat) * cosPhi - sin(decl))
				/ (cos(lat) * sin(phi));

		double az = arccos(cosAz);
		if (ha > 0)
			az = 2 * Math.PI - az;

		double alt1 = Math.toDegrees(Math.PI / 2 - phi);
		double az1 = Math.toDegrees(az);

		return new double[] { az1, alt1 };
	}

	private static double arccos(double x) {
		return Math.acos(x);
	}

	private static double cos(double a) {
		return Math.cos(a);
	}

	private static double sin(double a) {
		return Math.sin(a);
	}


	public static void main(String[] args) {
//		double lat = 12.0066228;
//		double lon = -79.8104811;
//		double timezone = -5.5;
		
//		double[] mirrorAngles = calcMirror(new double[] {10,45});
//		System.out.printf("%f %f\n", mirrorAngles[0], mirrorAngles[1]);
		
		/*
		 * for (int d = 0; d < 366; d++) { System.out.printf("%3d   ", d); for
		 * (int t = 6; t <= 18; t++) { double[] sun = calcSun(d, t, lat, lon,
		 * timezone); sun[0] = - (180 - sun[0]); double[] mirror =
		 * calcMirror(sun); System.out.printf("%2.2f %2.2f %2.2f %2.2f   ", ha,
		 * cosAz, mirror[0], mirror[1]); } System.out.println(); }
		 */
	}

}
