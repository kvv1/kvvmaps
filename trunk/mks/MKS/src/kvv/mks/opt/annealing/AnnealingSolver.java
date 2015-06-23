package kvv.mks.opt.annealing;

import kvv.mks.Solver;
import kvv.mks.State;
import kvv.mks.Util;
import kvv.mks.opt.TargetSumFunc;

public class AnnealingSolver implements Solver {

	private final TargetSumFunc targetFunc;
	private final double maxa;
	private final double maxd;

	public State state;

	private final Annealing<State> annealing = new Annealing<State>() {

		@Override
		public double getValue(State state) {
			return targetFunc.getValue(state);
		}

		double da = Util.g2r(2);
		double dd = 0.2;

		@Override
		public State getNextState(State state) {
			State state1 = new State(state);
			double d;
			d = Util.rand(-da, da);
			state1.ax += d;
			if (state1.ax > maxa || state1.ax < -maxa)
				state1.ax -= d;

			d = Util.rand(-da, da);
			state1.ay += d;
			if (state1.ay > maxa || state1.ay < -maxa)
				state1.ay -= d;

			d = Util.rand(-da, da);
			state1.az += d;
			if (state1.az > maxa || state1.az < -maxa)
				state1.az -= d;

			d = Util.rand(-dd, dd);
			state1.dx += d;
			if (state1.dx > maxd || state1.dx < -maxd)
				state1.dx -= d;

			d = Util.rand(-dd, dd);
			state1.dy += d;
			if (state1.dy > maxd || state1.dy < -maxd)
				state1.dy -= d;

			d = Util.rand(-dd, dd);
			state1.dz += d;
			if (state1.dz > maxd || state1.dz < -maxd)
				state1.dz -= d;

			return state1;
		}
	};

	public AnnealingSolver(TargetSumFunc targetFunc, double maxa, double maxd) {
		this.targetFunc = targetFunc;
		this.maxa = maxa;
		this.maxd = maxd;
	}

	@Override
	public State solve() {
		return annealing.run(new State());
	}

	@Override
	public String getAddPrint() {
		return "";
	}

}
