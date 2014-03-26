package kvv.kvvmap.adapter;

import android.graphics.Point;

public final class PointInt extends Point{

	public PointInt(int x, int y) {
		super(x, y);
	}

	public void setLocation(int x, int y) {
		super.set(x, y);
	}
	
	public void translate(int dx, int dy) {
		offset(dx, dy);
	}
}
