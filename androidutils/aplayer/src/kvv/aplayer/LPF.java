package kvv.aplayer;

public class LPF {
	private double k1;
	private double k2;
	private double acc;

	public LPF(int samplingRate, double t1, double t2) {
		k1 = Math.exp(-1.0 / samplingRate / t1);
		k2 = Math.exp(-1.0 / samplingRate / t2);
	}

	public double add(double v) {
		if (v > acc)
			acc = acc * k1 + v * (1 - k1);
		else
			acc = acc * k2 + v * (1 - k2);
		return acc;
	}
	
	public double get() {
		return acc;
	}
}