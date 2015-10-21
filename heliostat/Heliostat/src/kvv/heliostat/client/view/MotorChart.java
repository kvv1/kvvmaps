package kvv.heliostat.client.view;

import kvv.gwtutils.client.chart.Chart;
import kvv.gwtutils.client.chart.ChartData;
import kvv.heliostat.client.Heliostat;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;
import kvv.heliostat.shared.environment.Environment;
import kvv.simpleutils.spline.FunctionFactory;

public abstract class MotorChart extends Chart implements View {
	public double[][] data = new double[][] { new double[0], new double[0] };

	public MotorChart(Model model, double minx, double maxx, double stepx) {
		super(300, 200, minx, maxx, stepx, 0, Environment.MAX_STEPS, 10000,
				null);

		model.add(this);
	}

	private boolean eqArr(double[] a1, double[] a2) {
		if (a1.length != a2.length)
			return false;

		for (int i = 0; i < a1.length; i++)
			if (a1[i] != a2[i])
				return false;

		return true;
	}

	public void upd(double[][] data, double mark) {
		//if (!eqArr(this.data[0], data[0]) || !eqArr(this.data[1], data[1])) {
			this.data = data;

			ChartData cd3 = new ChartData(FunctionFactory.getFunction(data[0],
					data[1]), data[0], data[1], Heliostat.AZ_COLOR, mark);

			set(2, cd3);
		//}
	}

}
