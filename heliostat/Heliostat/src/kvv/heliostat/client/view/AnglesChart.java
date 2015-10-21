package kvv.heliostat.client.view;

import kvv.gwtutils.client.chart.ChartData;
import kvv.heliostat.client.Heliostat;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.shared.math.MirrorAngles;
import kvv.simpleutils.spline.Function;

public class AnglesChart extends TimeChart {

	private int day = -1;

	private Function az = new Function() {
		@Override
		public double value(double t) {
			return MirrorAngles.get(0, t).x;
		}
	};

	private Function az2 = new Function() {
		@Override
		public double value(double t) {
			return MirrorAngles.get(183, t).x;
		}
	};

	private Function alt = new Function() {
		@Override
		public double value(double t) {
			return MirrorAngles.get(0, t).y;
		}
	};

	private Function alt2 = new Function() {
		@Override
		public double value(double t) {
			return MirrorAngles.get(183, t).y;
		}
	};

	public AnglesChart(Model model) {
		super(model, 500, 160, 0, 24, 1, -60, 60, 10, null);

		set(0, new ChartData(az, Heliostat.AZ_COLOR_LIGHT, null));
		set(1, new ChartData(az2, Heliostat.AZ_COLOR_LIGHT, null));
		set(2, new ChartData(alt, Heliostat.ALT_COLOR_LIGHT, null));
		set(3, new ChartData(alt2, Heliostat.ALT_COLOR_LIGHT, null));
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
					return MirrorAngles.get(day, t).x;
				}
			};

			Function alt1 = new Function() {
				@Override
				public double value(double t) {
					return MirrorAngles.get(day, t).y;
				}
			};

			set(4, new ChartData(alt1, Heliostat.ALT_COLOR, null));
			set(5, new ChartData(az1, Heliostat.AZ_COLOR, null));
		}

	}
}
