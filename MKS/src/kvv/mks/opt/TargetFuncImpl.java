package kvv.mks.opt;

import java.util.Collection;

import kvv.mks.cloud.Pt;
import kvv.mks.cloud.PtGrid;

public class TargetFuncImpl implements TargetFunc{
	
	private final PtGrid model;
	private final double dist;
	
	public TargetFuncImpl(PtGrid model, double dist) {
		this.model = model;
		this.dist = dist;
	}
	
	public double getValue(double x, double y, double z) {
		int n = 0;
		
		Collection<Pt> candidates = model.getNeighbours(x, y, z, dist);
		if(candidates.size() > 0)
			n++;
		n += candidates.size();
		for (Pt pt2 : candidates) {
			if (pt2.dist(x, y, z) < dist / 2)
				n+=2;
			if (pt2.dist(x, y, z) < dist / 4)
				n+=4;
		}
		
		return n;
	}
}
