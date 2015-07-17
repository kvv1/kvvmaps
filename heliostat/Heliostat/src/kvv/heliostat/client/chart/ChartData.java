package kvv.heliostat.client.chart;

import kvv.heliostat.shared.spline.Function;

public class ChartData {

	public final Function function;
	public final double[] xx;
	public final double[] yy;
	public final String color;

	public ChartData(Function function, String color) {
		this(function, null, null, color);
	}

	public ChartData(double[] xx, double[] yy, String color) {
		this(null, xx, yy, color);
	}

	public ChartData(Function function, double[] xx, double[] yy,
			String color) {
		this.function = function;
		this.xx = xx;
		this.yy = yy;
		this.color = color;
	}

}