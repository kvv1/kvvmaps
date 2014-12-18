package kvv.mks.matrix;

import kvv.mks.cloud.Pt;

public class Matrix3x3 implements Rot {

	private double[][] elems = new double[3][3];

	Matrix3x3() {
		this(1, 0, 0, 0, 1, 0, 0, 0, 1);
	}

	private Matrix3x3(double a00, double a01, double a02, double a10,
			double a11, double a12, double a20, double a21, double a22) {
		elems[0][0] = a00;
		elems[0][1] = a01;
		elems[0][2] = a02;

		elems[1][0] = a10;
		elems[1][1] = a11;
		elems[1][2] = a12;

		elems[2][0] = a20;
		elems[2][1] = a21;
		elems[2][2] = a22;
	}

	@Override
	public double[] apply(double[] vec, double[] res) {
		if (res == null)
			res = new double[3];

		if (vec == res)
			throw new IllegalArgumentException();

		res[0] = vec[0] * elems[0][0] + vec[1] * elems[0][1] + vec[2]
				* elems[0][2];
		res[1] = vec[0] * elems[1][0] + vec[1] * elems[1][1] + vec[2]
				* elems[1][2];
		res[2] = vec[0] * elems[2][0] + vec[1] * elems[2][1] + vec[2]
				* elems[2][2];

		return res;
	}

	@Override
	public Pt apply(Pt pt, Pt res) {
		if (res == null)
			res = new Pt();

		if (pt == res)
			throw new IllegalArgumentException();

		res.x = pt.x * elems[0][0] + pt.y * elems[0][1] + pt.z * elems[0][2];
		res.y = pt.x * elems[1][0] + pt.y * elems[1][1] + pt.z * elems[1][2];
		res.z = pt.x * elems[2][0] + pt.y * elems[2][1] + pt.z * elems[2][2];

		return res;
	}

	@Override
	public Rot mul(Rot _m, Rot _res) {

		Matrix3x3 m = (Matrix3x3) _m;
		Matrix3x3 res = (Matrix3x3) _res;

		if (res == null)
			res = new Matrix3x3();

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				res.elems[i][j] = elems[i][0] * m.elems[0][j] + elems[i][1]
						* m.elems[1][j] + elems[i][2] * m.elems[2][j];

		return res;
	}

	private static Rot rotX(double a) {
		double cos = Math.cos(a);
		double sin = Math.sin(a);
		return new Matrix3x3(1, 0, 0, 0, cos, -sin, 0, sin, cos);
	}

	private static Rot rotY(double a) {
		double cos = Math.cos(a);
		double sin = Math.sin(a);
		return new Matrix3x3(cos, 0, sin, 0, 1, 0, -sin, 0, cos);
	}

	private static Rot rotZ(double a) {
		double cos = Math.cos(a);
		double sin = Math.sin(a);
		return new Matrix3x3(cos, -sin, 0, sin, cos, 0, 0, 0, 1);
	}

	public static Rot rot(double xrot, double yrot, double zrot) {
		Rot res = rotX(xrot);
		res = res.mul(rotY(yrot), res);
		res = res.mul(rotZ(zrot), res);
		return res;
	}

	public static Rot rot1(double xrot, double yrot, double zrot) {
		Rot res = rotZ(zrot);
		res = res.mul(rotY(yrot), res);
		res = res.mul(rotX(xrot), res);
		return res;
	}

}
