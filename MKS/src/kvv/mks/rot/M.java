package kvv.mks.rot;

import kvv.mks.rot.matrix.Matrix3x3;
import kvv.mks.rot.quaternion.Quaternion;

public abstract class M {

//	public static M instance = new M_Matrix();
	public static M instance = new M_Quaternion();

	public abstract Rot create();

	public abstract Rot rot(double xrot, double yrot, double zrot);
}

class M_Matrix extends M {
	public Rot create() {
		return new Matrix3x3();
	}

	public Rot rot(double xrot, double yrot, double zrot) {
		return Matrix3x3.rot(xrot, yrot, zrot);
	}
}

class M_Quaternion extends M {
	public Rot create() {
		return new Quaternion(1, 0, 0, 0);
	}

	public Rot rot(double xrot, double yrot, double zrot) {
		return Quaternion.rot(xrot, yrot, zrot);
	}
}
