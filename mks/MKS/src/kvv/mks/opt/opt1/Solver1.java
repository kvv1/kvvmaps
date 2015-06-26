package kvv.mks.opt.opt1;

import java.util.ArrayList;
import java.util.List;

import kvv.mks.Solver;
import kvv.mks.opt.TargetSumFunc;
import kvv.mks.rot.M;
import kvv.mks.rot.Transform;

public class Solver1 implements Solver {
	public Transform state = new Transform();

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
			Transform init) {
		this.targetFunc = targetFunc;
		if (init != null)
			state = init;
		
		optimizers.add(new Opt(maxAngle) {
			@Override
			public void incParam(double value) {
				state.rot = M.rot(value, 0, 0).mul(state.rot, null);
			}
		});

		optimizers.add(new Opt(maxAngle) {
			@Override
			public void incParam(double value) {
				state.rot = M.rot(0, value, 0).mul(state.rot, null);
			}
		});

		optimizers.add(new Opt(maxAngle) {
			@Override
			public void incParam(double value) {
				state.rot = M.rot(0, 0, value).mul(state.rot, null);
			}
		});

		optimizers.add(new Opt(maxDist) {
			@Override
			public void incParam(double value) {
				state.pt.x += value;
			}
		});

		optimizers.add(new Opt(maxDist) {
			@Override
			public void incParam(double value) {
				state.pt.y += value;
			}
		});

		optimizers.add(new Opt(maxDist) {
			@Override
			public void incParam(double value) {
				state.pt.z += value;
			}
		});
	}

	int steps;

	@Override
	public Transform solve() {
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
