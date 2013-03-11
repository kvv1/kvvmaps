package kvv.kvvmap.adapter;

import java.awt.Point;

public class PointInt extends Point {
	private static final long serialVersionUID = 1L;

	public PointInt(int x, int y) {
		super(x, y);
	}

	public PointInt(PointX px) {
		super((int) px.x, (int) px.y);
	}

	public void set(int x, int y) {
		setLocation(x, y);
	}
}
