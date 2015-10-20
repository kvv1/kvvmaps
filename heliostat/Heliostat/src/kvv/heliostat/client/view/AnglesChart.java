package kvv.heliostat.client.view;

import kvv.gwtutils.client.chart.ChartData;
import kvv.heliostat.client.Heliostat;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.environment.Environment;
import kvv.simpleutils.spline.Function;

public class AnglesChart extends TimeChart {

	private int day = -1;

	private Function az = new Function() {
		@Override
		public double value(double t) {
			return Environment.getMirrorAzimuth(0, t);
		}
	};

	private Function az2 = new Function() {
		@Override
		public double value(double t) {
			return Environment.getMirrorAzimuth(183, t);
		}
	};

	private Function alt = new Function() {
		@Override
		public double value(double t) {
			return Environment.getMirrorAltitude(0, t);
		}
	};

	private Function alt2 = new Function() {
		@Override
		public double value(double t) {
			return Environment.getMirrorAltitude(183, t);
		}
	};

	public AnglesChart(Model model) {
		super(model, 500, 160, 0, 24, 1, -60, 60, 10, null);

		set(0, new ChartData(az, Heliostat.AZ_COLOR_LIGHT));
		set(1, new ChartData(az2, Heliostat.AZ_COLOR_LIGHT));
		set(2, new ChartData(alt, Heliostat.ALT_COLOR_LIGHT));
		set(3, new ChartData(alt2, Heliostat.ALT_COLOR_LIGHT));
	}

	@Override
	public void updateView(HeliostatState state) {
		super.updateView(state);
		if (state == null)
			return;

		if (state.day != day) {
			day = state.day;

			Function az1 = new Function() {
				@Override
				public double value(double t) {
					return Environment.getMirrorAzimuth(day, t);
				}
			};

			Function alt1 = new Function() {
				@Override
				public double value(double t) {
					return Environment.getMirrorAltitude(day, t);
				}
			};

			set(4, new ChartData(alt1, Heliostat.ALT_COLOR));
			set(5, new ChartData(az1, Heliostat.AZ_COLOR));
		}

	}
}