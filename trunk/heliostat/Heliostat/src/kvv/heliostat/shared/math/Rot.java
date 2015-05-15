package kvv.heliostat.shared.math;


public interface Rot {
	double[] apply(double[] vec, double[] res);
	Rot mul(Rot m, Rot res);
	Rot getCopy();
	Rot inverse();
}
