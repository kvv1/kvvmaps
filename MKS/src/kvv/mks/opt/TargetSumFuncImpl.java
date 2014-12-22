package kvv.mks.opt;

import java.util.Collection;

import kvv.mks.cloud.Pt;
import kvv.mks.rot.Transform;

public class TargetSumFuncImpl implements TargetSumFunc {

	private final TargetFunc targetFunc;
	private final Collection<Pt> scan;

	public TargetSumFuncImpl(TargetFunc targetFunc, Collection<Pt> scan) {
		this.targetFunc = targetFunc;
		this.scan = scan;
	}

	public double getValue(Transform state) {
		int n = 0;

		Pt pt1 = new Pt();

		for (Pt pt : scan) {
			state.apply(pt, pt1);
			n += targetFunc.getValue(pt1.x, pt1.y, pt1.z);
		}
		return n;
	}
}
