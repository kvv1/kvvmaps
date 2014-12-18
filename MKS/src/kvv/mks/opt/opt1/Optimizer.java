package kvv.mks.opt.opt1;

public abstract class Optimizer {

	public abstract void setParam(double value);

	public abstract double getValue();

	private double arg;
	private double d;

	public Optimizer(double from, double to) {
		this.arg = (from + to) / 2;
		this.d = (from - to) / 2;
	}

	public double step() {
		setParam(arg + d);
		double valp = getValue();

		setParam(arg - d);
		double valm = getValue();

		setParam(arg);
		double val = getValue();

		if (valp > val && valp > valm) {
			arg += d;
			setParam(arg);
			return valp;
		} else if (valm > val && valm > valp) {
			arg -= d;
			setParam(arg);
			return valm;
		}
		
		return val;
	}

	public void narrow() {
		d /= 10;
	}

	public double getArg() {
		return arg;
	}

}
