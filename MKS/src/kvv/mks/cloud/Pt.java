package kvv.mks.cloud;

public class Pt {
	public double x;
	public double y;
	public double z;

	public Pt() {
	}

	public Pt(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Pt(Pt pt) {
		this(pt.x, pt.y, pt.z);
	}

	public final double dist(Pt other) {
		return dist(other.x, other.y, other.z);
	}

	public double dist(double x, double y, double z) {
		double dx = x - this.x;
		double dy = y - this.y;
		double dz = z - this.z;
		// return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz))/2;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public Pt rotTrans(double ax, double ay, double az, double x, double y,
			double z, Pt res) {
		if (res == null)
			res = new Pt();

		res.x = this.x;
		res.y = this.y;
		res.z = this.z;

		double cosx = Math.cos(ax);
		double sinx = Math.sin(ax);
		double cosy = Math.cos(ay);
		double siny = Math.sin(ay);
		double cosz = Math.cos(az);
		double sinz = Math.sin(az);

		res.rotateX(cosx, sinx, res);
		res.rotateY(cosy, siny, res);
		res.rotateZ(cosz, sinz, res);

		res.x += x;
		res.y += y;
		res.z += z;

		return res;
	}

	public Pt rotateX(double cos, double sin, Pt res) {
		if (res == null)
			res = new Pt();
		res.x = this.x;
		double y = this.y;
		double z = this.z;
		res.y = y * cos - z * sin;
		res.z = y * sin + z * cos;
		return res;
	}

	public Pt rotateY(double cos, double sin, Pt res) {
		if (res == null)
			res = new Pt();
		double x = this.x;
		double z = this.z;
		res.x = x * cos + z * sin;
		res.y = this.y;
		res.z = -x * sin + z * cos;
		return res;
	}

	public Pt rotateZ(double cos, double sin, Pt res) {
		if (res == null)
			res = new Pt();
		double x = this.x;
		double y = this.y;
		res.z = this.z;
		res.x = x * cos - y * sin;
		res.y = x * sin + y * cos;
		return res;
	}

}