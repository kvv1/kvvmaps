package kvv.kvvmap.adapter;


public final class RectX {

	private double x;
	private double y;
	private double w;
	private double h;

	public RectX(double x, double y, double w, double h) {
		set(x, y, w, h);
	}

	public void set(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getWidth() {
		return w;
	}

	public double getHeight() {
		return h;
	}

	public boolean intersects(double x, double y, double w, double h) {
		if (this.w <= 0 || this.h <= 0 || w <= 0 || h <= 0) {
			return false;
		}
		double x0 = getX();
		double y0 = getY();
		return (x + w > x0 && y + h > y0 && x < x0 + this.w && y < y0 + this.h);
	}

	public final boolean intersects(RectX r) {
		return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public void union(double newx, double newy) {
		double x1 = Math.min(x, newx);
		double x2 = Math.max(x + w, newx);
		double y1 = Math.min(y, newy);
		double y2 = Math.max(y + h, newy);
		set(x1, y1, x2 - x1, y2 - y1);
	}

	public void offset(double x, double y) {
		this.x += x;
		this.y += y;
	}

	public void inset(double x, double y) {
		this.x += x;
		this.y += y;
		this.w -= 2 * x;
		this.h -= 2 * y;
	}

	public boolean contains(double x, double y) {
		return x >= this.x && x < this.x + this.w && y >= this.y
				&& y < this.y + this.h;
	}
}
