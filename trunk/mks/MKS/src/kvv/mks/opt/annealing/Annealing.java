package kvv.mks.opt.annealing;

public abstract class Annealing<State> {

	double T = 100;
	double TMin = 3;
	double K = 0.999;

	public abstract double getValue(State state);

	public abstract State getNextState(State state);

	public State run(State state) {

		double t = T;

		double val = getValue(state);

		while (t >= TMin) {
			State state1 = getNextState(state);
			double val1 = getValue(state1);
			double dE = val - val1;
			if (dE <= 0) {
				state = state1;
				val = val1;
				//System.out.println(dE + "<0");
			} else {
				double p = Math.exp(-dE / t);
				//System.out.println(dE + ">0 p=" + p);
				
				if (p > Math.random()) {
					state = state1;
					val = val1;
				}
			}

			t *= K;
		}

		return state;
	}

}
