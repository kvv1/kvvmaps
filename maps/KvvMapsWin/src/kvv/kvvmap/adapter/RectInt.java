package kvv.kvvmap.adapter;

import java.awt.Rectangle;

public class RectInt {
	public final Rectangle rect = new Rectangle();

	public void set(int x, int y, int w, int h) {
		rect.x = x;
		rect.y = y;
		rect.width = w;
		rect.height = h;
	}

	public int getX() {
		return rect.x;
	}

	public int getY() {
		return rect.y;
	}

	public int getW() {
		return rect.width;
	}

	public int getH() {
		return rect.height;
	}
}
