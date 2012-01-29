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

}
