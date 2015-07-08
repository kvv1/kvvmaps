package kvv.heliostat.client;

import kvv.heliostat.client.chart.Chart;
import kvv.heliostat.shared.environment.Environment;
import kvv.heliostat.shared.spline.Function;

public class AnglesChart  extends Chart{

	public AnglesChart() {
		super(600, 200, 0, 24, 1, -60, 60, 10, true);
		
		Function az = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAzimuth(0, t);
			}
		};

		Function az1 = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAzimuth(91, t);
			}
		};

		Function az2 = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAzimuth(182, t);
			}
		};

		Function alt = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAltitude(0, t);
			}
		};

		Function alt1 = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAltitude(91, t);
			}
		};

		Function alt2 = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAltitude(182, t);
			}
		};

		set(new ChartData(az, "cyan"), new ChartData(az1, "cyan"),
				new ChartData(az2, "cyan"), new ChartData(alt, "LawnGreen"),
				new ChartData(alt1, "LawnGreen"), new ChartData(alt2,
						"LawnGreen"));
	}

}
