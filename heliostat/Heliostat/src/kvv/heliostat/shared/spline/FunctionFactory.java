package kvv.heliostat.shared.spline;

public class FunctionFactory {
	public static Function getFunction(final double[] args, final double[] vals) {
		if(args.length == 0)
			return null;
		
		if (args.length == 1)
			return new Function() {
				@Override
				public double value(double v) {
					return vals[0];
				}
			};
		if (args.length == 2)
			return new Function() {
				@Override
				public double value(double v) {
					return vals[0] + (v - args[0]) * (vals[1] - vals[0])
							/ (args[1] - args[0]);
				}
			};

		return new SplineInterpolator().interpolate(args, vals);
	}

	public static double solve(Function f, double y, double x1, double x2,
			double e) {

		double y1 = f.value(x1);
		if(x2 == 60)
			x2 = x2;
		double y2 = f.value(x2);
		for (;;) {
			if (y > y1 && y > y2 || y < y1 && y < y2)
				throw new IllegalArgumentException();

			double xc = (x1 + x2) / 2;
			if (x2 - x1 < e)
				return xc;

			double yc = f.value(xc);

			if (yc > y == y2 > y1) {
				x2 = xc;
				y2 = yc;
			} else {
				x1 = xc;
				y1 = yc;
			}
		}
	}

	public static double q(double t, double p0, double p1, double p2, double p3) {
		return 0.5 * ((2 * p1) + (-p0 + p2) * t
				+ (2 * p0 - 5 * p1 + 4 * p2 - p3) * (t * t) + (-p0 + 3 * p1 - 3
				* p2 + p3)
				* (t * t * t));
	}

	public static double linInterpol(double x1, double y1, double x2,
			double y2, double x) {
		return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
	}
}
