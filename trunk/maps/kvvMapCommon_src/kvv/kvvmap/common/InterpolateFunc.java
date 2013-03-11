package kvv.kvvmap.common;

public abstract class InterpolateFunc {
	private final double[] data;
	private final double from;
	private final double to;

	public InterpolateFunc(int sz, double from, double to) {
		this.from = from;
		this.to = to;
		data = new double[sz];
		for (int i = 0; i < sz; i++)
			data[i] = refFunc(from + (to - from) * i / (sz - 1));
	}

	protected abstract double refFunc(double arg);

	public double func(double arg) {
		double fidx = (arg - from) * (data.length - 1) / (to - from);
		int idx1 = (int) Math.floor(fidx);
		if (idx1 < 0)
			idx1 = 0;
		if (idx1 >= data.length - 1)
			idx1 = data.length - 2;
		return data[idx1] + (data[idx1 + 1] - data[idx1]) * (fidx - idx1);
	}
}
