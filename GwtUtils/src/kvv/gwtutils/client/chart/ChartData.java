package kvv.gwtutils.client.chart;

import kvv.simpleutils.spline.Function;

public class ChartData {

	public final Function function;
	public final double[] xx;
	public final double[] yy;
	public final String color;
	public final Double mark;

	public ChartData(Function function, String color, Double mark) {
		this(function, null, null, color, mark);
	}

	public ChartData(double[] xx, double[] yy, String color, Double mark) {
		this(null, xx, yy, color, mark);
	}

	public ChartData(Function function, double[] xx, double[] yy,
			String color, Double mark) {
		this.function = function;
		this.xx = xx;
		this.yy = yy;
		this.color = color;
		this.mark = mark;
	}

}