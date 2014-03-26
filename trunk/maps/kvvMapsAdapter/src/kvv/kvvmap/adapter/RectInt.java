package kvv.kvvmap.adapter;

import android.graphics.Rect;

public class RectInt {
	public final Rect rect = new Rect();

	public void set(int x, int y, int w, int h) {
		rect.set(x, y, x + w, y + h);
	}

	public int getX() {
		return rect.left;
	}

	public int getY() {
		return rect.top;
	}
	
	public int getW() {
		return rect.width();
	}
	
	public int getH() {
		return rect.height();
	}
}
