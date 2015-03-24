package kvv.aplayer;

public class LPF {
	private final double k1;
	private final double k2;
	private double acc;

	public LPF(int samplingRate, double t1, double t2) {
		k1 = Math.exp(-1.0 / samplingRate / t1);
		k2 = Math.exp(-1.0 / samplingRate / t2);
	}

	public synchronized double add(double v) {
		if (v > acc)
			acc = acc * k1 + v * (1 - k1);
		else
			acc = acc * k2 + v * (1 - k2);
		return acc;
	}
	
	public synchronized void set(double v) {
		acc = v;
	}
	
	public synchronized double get() {
		return acc;
	}
}