package kvv.mks.opt.opt1;

import java.util.ArrayList;
import java.util.List;

import kvv.mks.Solver;
import kvv.mks.opt.State;
import kvv.mks.opt.TargetSumFunc;
import kvv.mks.rot.M;

public class Solver1 implements Solver {
	public State state = new State(M.instance.rot(0, 0, 0), 0, 0, 0);

	private final TargetSumFunc targetFunc;

	private final List<Optimizer> optimizers = new ArrayList<>();

	private double minValue = Double.MAX_VALUE;

	public double getValue() {
		return targetFunc.getValue(state);
	}

	abstract class Opt extends Optimizer {
		public Opt(double d) {
			super(d);
		}

		@Override
		public double getValue() {
			double val = Solver1.this.getValue();
			if (val < minValue)
				minValue = val;
			return val;
		}
	}

	public Solver1(double maxAngle, double maxDist, TargetSumFunc targetFunc,
			State init) {
		this.targetFunc = targetFunc;
		if (init != null)
			state = init;
		
//		state.rot = M.instance.rot(0.1, 0, 0).mul(state.rot, null);
//		state.rot = M.instance.rot(-0.2, 0, 0).mul(state.rot, null);
//		state.rot = M.instance.rot(0.1, 0, 0).mul(state.rot, null);
		

		optimizers.add(new Opt(maxAngle) {
			@Override
			public void incParam(double value) {
				state.rot = M.instance.rot(value, 0, 0).mul(state.rot, null);
//				state.rot = state.rot.mul(M.instance.rot(value, 0, 0), null);
			}
		});

		optimizers.add(new Opt(maxAngle) {
			@Override
			public void incParam(double value) {
				state.rot = M.instance.rot(0, value, 0).mul(state.rot, null);
//				state.rot = state.rot.mul(M.instance.rot(0,value,  0), null);
			}
		});

		optimizers.add(new Opt(maxAngle) {
			@Override
			public void incParam(double value) {
				state.rot = M.instance.rot(0, 0, value).mul(state.rot, null);
//				state.rot = state.rot.mul(M.instance.rot(0,0,value), null);
			}
		});

		optimizers.add(new Opt(maxDist) {
			@Override
			public void incParam(double value) {
				state.dx += value;
			}
		});

		optimizers.add(new Opt(maxDist) {
			@Override
			public void incParam(double value) {
				state.dy += value;
			}
		});

		optimizers.add(new Opt(maxDist) {
			@Override
			public void incParam(double value) {
				state.dz += value;
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
