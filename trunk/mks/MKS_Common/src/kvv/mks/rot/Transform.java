package kvv.mks.rot;

import kvv.mks.cloud.Pt;

public class Transform {
	public Rot rot;
	public Pt pt;

	public Transform() {
		this(M.rot(0, 0, 0), new Pt());
	}

	public Transform(Transform state) {
		this(state.rot.getCopy(), new Pt(state.pt));
	}

	public Transform(Rot rot, Pt pt) {
		this.rot = rot;
		this.pt = pt;
	}

	//TODO
	public Transform(Rot matrix, double x, double y, double z) {
		this(matrix, new Pt(x, y, z));
	}

	@Override
	public String toString() {
		return String
				.format("%s %7.3f %7.3f %7.3f", rot.toString(), pt.x, pt.y, pt.z);
	}

	public Pt relToAbs(Pt pt, Pt res) {
		if (res == null)
			res = new Pt();
		rot.apply(pt, res);

		res.x += this.pt.x;
		res.y += this.pt.y;
		res.z += this.pt.z;

		return res;
	}

	public Pt absToRel(Pt pt, Pt res) {
		if (res == null)
			res = new Pt(pt);

		res.x = pt.x - this.pt.x;
		res.y = pt.y - this.pt.y;
		res.z = pt.z - this.pt.z;

		return rot.inverse().apply(res, res);
	}

	//TODO
	public Pt apply(Pt pt, Pt res) {
		//return relToAbs(pt, res);
		return absToRel(pt, res);
	}
}