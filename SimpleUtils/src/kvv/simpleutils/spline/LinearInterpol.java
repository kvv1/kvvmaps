package kvv.simpleutils.spline;

import java.util.Arrays;

public class LinearInterpol implements Function {

	private final double[] xx;
	private final double[] yy;

	public LinearInterpol(double[] xx, double[] yy) {
		this.xx = xx;
		this.yy = yy;
	}

	@Override
	public double value(double v) {

		int i = Arrays.binarySearch(xx, v);
		if (i < 0) {
			i = -i - 2;
		}

		if (i < 0)
			i = 0;
		if (i == xx.length-1)
			i--;

		return yy[i] + (v - xx[i]) * (yy[i + 1] - yy[i]) / (xx[i + 1] - xx[i]);
	}

}
