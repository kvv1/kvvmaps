package kvv.kvvmap.common.pacemark;

import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.COLOR;

public class PathDrawer {

	public static void drawLabel(GC gc, String text, int x, int y) {
		RectX rect = gc.getTextBounds(text);
		rect.offset(x, y);
		rect.inset(-2, -2);
		gc.setColor(0x80000000);
		gc.fillRect((int) rect.getX(), (int) rect.getY(),
				(int) (rect.getX() + rect.getWidth()),
				(int) (rect.getY() + rect.getHeight()));
		gc.setTextSize(16);
		gc.setColor(COLOR.CYAN);
//		gc.drawText(text, x+1, y+1);
//		gc.setColor(COLOR.BLUE);
		gc.drawText(text, x, y);
	}

}
