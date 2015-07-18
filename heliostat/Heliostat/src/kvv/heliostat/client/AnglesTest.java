package kvv.heliostat.client;

import kvv.heliostat.client.chart.Chart;
import kvv.heliostat.client.chart.ChartData;
import kvv.heliostat.client.panel.VertPanel;
import kvv.heliostat.shared.math.MirrorAngles;
import kvv.heliostat.shared.spline.Function;

import com.google.gwt.user.client.ui.Composite;

public class AnglesTest extends Composite {

	Chart dateChart = new Chart(1000, 20, 0, 12, 1, 0, 1, 1, null) {
		protected void onClick(double arg) {
			reset((int) (arg * 365 / 12));
			setCursor(arg);
		};
	};

	Chart chart = new Chart(1000, 600, 0, 24, 1, -180, 180, 10, new double[] {
			-90, 0, 90 });

	{
		initWidget(new VertPanel(dateChart, new Gap(10, 10), chart));
		reset(0);
	}

	void reset(final int day) {
		chart.set(0, new ChartData(new Function() {
			@Override
			public double value(double v) {
				double az = MirrorAngles.calcSun(day, v, MirrorAngles.LAT,
						MirrorAngles.LON, MirrorAngles.TIMEZONE)[0];
				return az - 180;
			}
		}, "lightskyblue"));

		chart.set(1, new ChartData(new Function() {
			@Override
			public double value(double v) {
				return MirrorAngles.calcSun(day, v, MirrorAngles.LAT,
						MirrorAngles.LON, MirrorAngles.TIMEZONE)[1];
			}
		}, "lightgreen"));

		chart.set(2, new ChartData(new Function() {
			@Override
			public double value(double v) {
				double[] sun = MirrorAngles.calcSun(day, v, MirrorAngles.LAT,
						MirrorAngles.LON, MirrorAngles.TIMEZONE);
				sun[0] = -(180 - sun[0]);
				double[] mirror = MirrorAngles.calcMirror(sun);
				return mirror[0];
			}
		}, "deepskyblue"));

		chart.set(3, new ChartData(new Function() {
			@Override
			public double value(double v) {
				double[] sun = MirrorAngles.calcSun(day, v, MirrorAngles.LAT,
						MirrorAngles.LON, MirrorAngles.TIMEZONE);
				sun[0] = -(180 - sun[0]);
				double[] mirror = MirrorAngles.calcMirror(sun);
				return mirror[1];
			}
		}, "limegreen"));
	}

}
