package kvv.kvvmap.adapter;

import android.graphics.Rect;
import android.graphics.RectF;

public final class RectX extends RectF {

	public RectX(float x, float y, float w, float h) {
		super(x, y, x + w, y + h);
	}

	public RectX(double x, double y, double w, double h) {
		this((float) x, (float) y, (float) w, (float) h);
	}

	public RectX(Rect r) {
		super(r.left, r.top, r.right, r.bottom);
	}

	public RectX(RectF r) {
		super(r.left, r.top, r.right, r.bottom);
	}

	private static final int OUT_LEFT = 1;
	private static final int OUT_TOP = 2;
	private static final int OUT_RIGHT = 4;
	private static final int OUT_BOTTOM = 8;

	private int outcode(double x, double y) {
		int out = 0;
		if (this.right - this.left <= 0) {
			out |= OUT_LEFT | OUT_RIGHT;
		} else if (x < this.left) {
			out |= OUT_LEFT;
		} else if (x > this.right) {
			out |= OUT_RIGHT;
		}
		if (this.bottom - this.top <= 0) {
			out |= OUT_TOP | OUT_BOTTOM;
		} else if (y < this.top) {
			out |= OUT_TOP;
		} else if (y > this.bottom) {
			out |= OUT_BOTTOM;
		}
		return out;
	}

	public final boolean intersectsLine(double x1, double y1, double x2,
			double y2) {
		int out1, out2;
		if ((out2 = outcode(x2, y2)) == 0) {
			return true;
		}
		while ((out1 = outcode(x1, y1)) != 0) {
			if ((out1 & out2) != 0) {
				return false;
			}
			if ((out1 & (OUT_LEFT | OUT_RIGHT)) != 0) {
				double x = super.left;
				if ((out1 & OUT_RIGHT) != 0) {
					x += super.right - super.left;
				}
				y1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
				x1 = x;
			} else {
				double y = super.top;
				if ((out1 & OUT_BOTTOM) != 0) {
					y += super.bottom - super.top;
				}
				x1 = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
				y1 = y;
			}
		}
		return true;
	}

	public final boolean contains(PointX pt) {
		return super.contains(pt.x, pt.y);
	}

	public final void union(double x, double y) {
		super.union((float) x, (float) y);
	}

	public final void union(float x, float y) {
		super.union(x, y);
	}

	public final boolean intersects(RectX rect) {
		return intersects(this, rect);
	}

	public final boolean contains(double x, double y) {
		return super.contains((float) x, (float) y);
	}

	public final double getX() {
		return super.left;
	}

	public final double getY() {
		return super.top;
	}

	public final double getWidth() {
		return super.right - super.left;
	}

	public final double getHeight() {
		return super.bottom - super.top;
	}

	public void set(double x, double y, double w, double h) {
		super.set((float) x, (float) y, (float) (x + w), (float) (y + h));
	}
}
