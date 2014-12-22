package kvv.mks;

import kvv.mks.rot.Transform;


public interface Solver {
	public Transform solve();
	public String getAddPrint();
}
