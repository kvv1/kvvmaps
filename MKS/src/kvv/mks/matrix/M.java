package kvv.mks.matrix;

import kvv.mks.q.Quaternion;

public abstract class M {

	public static M instance = new M_Matrix();

	public abstract Rot create();

	public abstract Rot rot(double xrot, double yrot, double zrot);
	public abstract Rot rot1(double xrot, double yrot, double zrot);

	// public static Rot rotX(double a) {
	// return Quaternion.rotX(a);
	// //return Matrix3x3.rotX(a);
	// }
	//
	// public static Rot rotY(double a) {
	// return Quaternion.rotY(a);
	// //return Matrix3x3.rotY(a);
	// }

}

class M_Matrix extends M {
	public Rot create() {
		return new Matrix3x3();
	}

	public Rot rot(double xrot, double yrot, double zrot) {
		return Matrix3x3.rot(xrot, yrot, zrot);
	}

	public Rot rot1(double xrot, double yrot, double zrot) {
		return Matrix3x3.rot1(xrot, yrot, zrot);
	}
}

class M_Quaternion extends M {
	public Rot create() {
		return new Quaternion(1, 0, 0, 0);
	}

	public Rot rot(double xrot, double yrot, double zrot) {
		return Quaternion.rot(xrot, yrot, zrot);
	}

	public Rot rot1(double xrot, double yrot, double zrot) {
		return Quaternion.rot(xrot, yrot, zrot);
	}
}
