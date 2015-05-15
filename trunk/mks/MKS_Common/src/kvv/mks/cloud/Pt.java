package kvv.mks.cloud;

public class Pt {
	public double x;
	public double y;
	public double z;

	@Override
	public String toString() {
		return String.format("%.2f %.2f %.2f", x, y, z);
	}

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

	public double mod() {
		return Math.sqrt(x * x + y * y + z * z);
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

	public Pt norm() {
		double mod = Math.sqrt(x * x + y * y + z * z);
		return new Pt(x / mod, y / mod, z / mod);
	}

	public Pt inverse() {
		return new Pt(-x, -y, -z);
	}

	public Pt add(Pt pt) {
		return new Pt(x + pt.x, y + pt.y, z + pt.z);
	}

}