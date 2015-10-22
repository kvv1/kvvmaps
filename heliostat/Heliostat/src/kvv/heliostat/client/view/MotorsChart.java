package kvv.heliostat.client.view;

import kvv.gwtutils.client.chart.Chart;
import kvv.gwtutils.client.chart.ChartData;
import kvv.heliostat.client.Heliostat;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;
import kvv.heliostat.shared.environment.Environment;
import kvv.heliostat.shared.math.MirrorAngles;
import kvv.simpleutils.spline.FunctionFactory;

public class MotorsChart extends Chart implements View {
	public double[][] azData = new double[][] { new double[0], new double[0] };
	public double[][] altData = new double[][] { new double[0], new double[0] };

	public MotorsChart(Model model) {
		super(500, 200, -60, 60, 10, 0, Environment.MAX_STEPS, 10000, null);

		model.add(this);

		// ChartData cd1 = new ChartData(Environment.azDeg2Steps,
		// Heliostat.AZ_COLOR_LIGHT);
		// ChartData cd2 = new ChartData(Environment.altDeg2Steps,
		// Heliostat.ALT_COLOR_LIGHT);
		//
		// set(0, cd1);
		// set(1, cd2);
	}

	private boolean eqArr(double[] a1, double[] a2) {
		if (a1.length != a2.length)
			return false;

		for (int i = 0; i < a1.length; i++)
			if (a1[i] != a2[i])
				return false;

		return true;
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;

		if (!eqArr(azData[0], state.azData[0])
				|| !eqArr(azData[1], state.azData[1])
				|| !eqArr(altData[0], state.altData[0])
				|| !eqArr(altData[1], state.altData[1])) {
			azData = state.azData;
			altData = state.altData;

			ChartData cd3 = new ChartData(FunctionFactory.getFunction(
					state.azData[0], state.azData[1]), state.azData[0],
					state.azData[1], Heliostat.AZ_COLOR, MirrorAngles.get(
							state.dayTime.day, state.dayTime.time).x);
			
			ChartData cd4 = new ChartData(FunctionFactory.getFunction(
					state.altData[0], state.altData[1]), state.altData[0],
					state.altData[1], Heliostat.ALT_COLOR, MirrorAngles.get(
							state.dayTime.day, state.dayTime.time).y);

			set(2, cd3);
			set(3, cd4);

			// set(cd1, cd2, cd3, cd4);
		}

	}
}
