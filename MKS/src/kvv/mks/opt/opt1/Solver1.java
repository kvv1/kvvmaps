package kvv.mks.opt.opt1;

import java.util.ArrayList;
import java.util.List;

import kvv.mks.Solver;
import kvv.mks.State;
import kvv.mks.opt.TargetSumFunc;

public class Solver1 implements Solver {
	public State state = new State();

	private final TargetSumFunc targetFunc;

	private final List<Optimizer> optimizers = new ArrayList<>();

	private double minValue = Double.MAX_VALUE;

	public double getValue() {
		return targetFunc.getValue(state);
	}

	abstract class Opt extends Optimizer {
		public Opt(double from, double to) {
			super(from, to);
		}

		@Override
		public double getValue() {
			double val = Solver1.this.getValue();
			if (val < minValue)
				minValue = val;
			return val;
		}
	}

	public Solver1(double maxAngle, double maxDist, TargetSumFunc targetFunc, State init) {
		this.targetFunc = targetFunc;
		if(init != null)
			state = init;

		optimizers.add(new Opt(-maxAngle, maxAngle) {
			@Override
			public void setParam(double value) {
				state.ax = value;
			}
		});

		optimizers.add(new Opt(-maxAngle, maxAngle) {
			@Override
			public void setParam(double value) {
				state.ay = value;
			}
		});

		optimizers.add(new Opt(-maxAngle, maxAngle) {
			@Override
			public void setParam(double value) {
				state.az = value;
			}
		});

		optimizers.add(new Opt(-maxDist, maxDist) {
			@Override
			public void setParam(double value) {
				state.dx = value;
			}
		});

		optimizers.add(new Opt(-maxDist, maxDist) {
			@Override
			public void setParam(double value) {
				state.dy = value;
			}
		});

		optimizers.add(new Opt(-maxDist, maxDist) {
			@Override
			public void setParam(double value) {
				state.dz = value;
			}
		});
	}

	int steps;

	@Override
	public State solve() {
		double lastValue = Double.MIN_VALUE;

		steps = 0;

		double prev;
		double prev1;

		do {
			prev1 = lastValue;
			do {
				prev = lastValue;
				for (Optimizer opt : optimizers)
					lastValue = opt.step();
				steps++;
			} while (lastValue > prev);

			for (Optimizer opt : optimizers)
				opt.narrow();
		} while (lastValue > prev1);

		return state;
	}

	@Override
	public String getAddPrint() {
		return String.format("%2d %5d", steps, (int) minValue);
	}

}
