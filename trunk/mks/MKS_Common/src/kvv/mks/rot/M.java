package kvv.mks.rot;

import java.util.ArrayList;
import java.util.List;

import kvv.mks.Util;
import kvv.mks.cloud.Pt;
import kvv.mks.rot.quaternion.Quaternion;

public abstract class M {
	public static Rot rot(double xrot, double yrot, double zrot) {
		// return Matrix3x3.rot(xrot, yrot, zrot);
		return Quaternion.rot(xrot, yrot, zrot);
	}

	public static List<Rot> directions = new ArrayList<>();
	static {
		// for (int x : new int[] { -1, 1 })
		// for (int y : new int[] { -1, 1 })
		// for (int z : new int[] { -1, 1 })
		for (int x : new int[] { -1, 0, 1 })
			for (int y : new int[] { -1, 0, 1 })
				for (int z : new int[] { -1, 0, 1 })
					if (x != 0 || y != 0 || z != 0) {
						Quaternion fromDir = Quaternion
								.fromDir(new Pt(x, y, z));
						for (int a = 0; a < 360; a += 45) {
							Rot rot = rot(0, Util.g2r(a), 0);
							directions.add(fromDir.mul(rot, null));
						}
					}
	}
}
