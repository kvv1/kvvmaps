package kvv.mks.matrix;

import kvv.mks.cloud.Pt;

public interface Rot {
	double[] apply(double[] vec, double[] res);
	Pt apply(Pt pt, Pt res);
	Rot mul(Rot m, Rot res);

	

}
