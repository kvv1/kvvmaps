package kvv.mks.opt.opt1;

public abstract class Optimizer {

	public abstract void incParam(double value);

	public abstract double getValue();

	private double d;

	public Optimizer(double d) {
		this.d = d;
	}

	public double step() {
		double val = getValue();

		incParam(d);
		double valp = getValue();

		incParam(-2 * d);
		double valm = getValue();

		if (valp > val && valp > valm) {
			incParam(2 * d);
			return valp;
		} else if (valm > val && valm > valp) {
			return valm;
		}

		incParam(d);
		return val;
	}

	public void narrow() {
		d /= 10;
	}

}
