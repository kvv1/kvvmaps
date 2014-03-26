package kvv.kvvmap.adapter;

import java.awt.geom.Rectangle2D;

public class RectX extends Rectangle2D.Double {
	private static final long serialVersionUID = 1L;

	public RectX(int x, int y, int w, int h) {
		super(x, y, w, h);
	}

	public RectX(double x, double y, double w, double h) {
		super(x, y, w, h);
	}

	public RectX(Rectangle2D r) {
		super(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public final void union(double x, double y) {
		super.add((float) x, (float) y);
	}

	public float centerX() {
		return (float) this.getCenterX();
	}

	public float centerY() {
		return (float) this.getCenterY();
	}

	public void offset(double dx, double dy) {
		super.setRect(getX() + dx, getY() + dy, getWidth(), getHeight());
	}

	public void inset(double dx, double dy) {
		super.setRect(getX() + dx, getY() + dy, getWidth() - dx - dx,
				getHeight() - dy - dy);
	}

	public void set(Rectangle2D.Double r) {
		setRect(r);
	}

	public void set(double x, double y, double w, double h) {
		super.setRect(x, y, w, h);
	}
}
