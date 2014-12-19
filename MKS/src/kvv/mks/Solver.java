package kvv.mks;

import kvv.mks.opt.State;


public interface Solver {
	public State solve();
	public String getAddPrint();
}
