package kvv.mks.rot.quaternion;

import kvv.mks.cloud.Pt;
import kvv.mks.rot.Rot;

public class Quaternion implements Rot {
	public double r, i, j, k;

	public Quaternion(double r, double i, double j, double k) {
		this.r = r;
		this.i = i;
		this.j = j;
		this.k = k;
	}

	@Override
	public Rot inverse() {
		return inverse(null);
	};

	public double mag() {
		return Math.sqrt(r * r + i * i + j * j + k * k);
	}

	public Quaternion inverse(Quaternion res) {
		if (res == null)
			res = new Quaternion(0, 0, 0, 0);

		double mag = this.mag();

		res.r = r / mag;
		res.i = -i / mag;
		res.j = -j / mag;
		res.k = -k / mag;

		return res;
	}

	public Quaternion unit(Quaternion res) {
		if (res == null)
			res = new Quaternion(0, 0, 0, 0);

		double mag = this.mag();

		res.r = r;
		res.i = i / mag;
		res.j = j / mag;
		res.k = k / mag;

		return res;
	}

	@Override
	public Rot mul(Rot m, Rot _res) {
		Quaternion q = (Quaternion) m;
		Quaternion res = (Quaternion) _res;
		if (res == null)
			res = new Quaternion(0, 0, 0, 0);

		double r = this.r * q.r - this.i * q.i - this.j * q.j - this.k * q.k;
		double i = this.r * q.i + this.i * q.r + this.j * q.k - this.k * q.j;
		double j = this.r * q.j - this.i * q.k + this.j * q.r + this.k * q.i;
		double k = this.r * q.k + this.i * q.j - this.j * q.i + this.k * q.r;

		res.r = r;
		res.i = i;
		res.j = j;
		res.k = k;

		return res;
	}

	@Override
	public double[] apply(double[] vec, double[] res) {
		// TODO Auto-generated method stub
		return null;
	}

	static Quaternion p = new Quaternion(0, 0, 0, 0);
	static Quaternion qres = new Quaternion(0, 0, 0, 0);

	@Override
	public Pt apply(Pt pt, Pt res) {
		if (res == null)
			res = new Pt();

		p.i = pt.x;
		p.j = pt.y;
		p.k = pt.z;

		inverse(qres);

		p.mul(qres, qres);
		mul(qres, qres);

		res.x = qres.i;
		res.y = qres.j;
		res.z = qres.k;

		// r = q.mul(p.mul(q.inverse()));

		return res;
	}

	public static Rot rotX(double a) {
		Quaternion q = new Quaternion(0, 0, 0, 0);
		q.set(a, 0, 0);
		return q;
	}

	public static Rot rotY(double a) {
		Quaternion q = new Quaternion(0, 0, 0, 0);
		q.set(0, a, 0);
		return q;
	}

	public static Rot rot(double xrot, double yrot, double zrot) {
		Quaternion q = new Quaternion(0, 0, 0, 0);
		q.set(xrot, yrot, zrot);
		return q;
	}

	private void set(double xrot, double yrot, double zrot) {
		double angle;
		double sx, sy, sz, cx, cy, cz;

		angle = zrot * 0.5;
		sz = Math.sin(angle);
		cz = Math.cos(angle);

		angle = yrot * 0.5;
		sy = Math.sin(angle);
		cy = Math.cos(angle);

		angle = xrot * 0.5;
		sx = Math.sin(angle);
		cx = Math.cos(angle);

		i = (sx * cy * cz - cx * sy * sz);
		j = (cx * sy * cz + sx * cy * sz);
		k = (cx * cy * sz - sx * sy * cz);
		r = (cx * cy * cz + sx * sy * sz);

		unit(this);
	}

	@Override
	public Rot getCopy() {
		return new Quaternion(r, i, j, k);
	}

	@Override
	public double dist(Rot r) {
		Rot d = mul(r.inverse(), null);
		Pt pt = new Pt(1,1,1);
		Pt pt1 = d.apply(pt, null);
		
		return pt.dist(pt1);
	}

}