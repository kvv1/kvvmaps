package kvv.mks.q;

import kvv.mks.cloud.Pt;
import kvv.mks.matrix.Rot;

public class Quaternion implements Rot {
	public double r, i, j, k;

	public Quaternion(double r, double i, double j, double k) {
		this.r = r;
		this.i = i;
		this.j = j;
		this.k = k;
	}

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

	@Override
	public Pt apply(Pt pt, Pt res) {
		if (res == null)
			res = new Pt();

		Quaternion p = new Quaternion(0, pt.x, pt.y, pt.z);

		Quaternion r = new Quaternion(0, 0, 0, 0);
		inverse(r);

		p.mul(r, r);
		mul(r, r);

		res.x = r.i;
		res.y = r.j;
		res.z = r.k;

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
	};
}