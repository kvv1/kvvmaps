package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.Utils;

public class ViewHelper {
	public static void drawText(GC gc, String text, int x, int y,
			int textColor, int backgroundColor) {
		RectX bounds = gc.getTextBounds(text);
		bounds.offset(x, y);
		bounds.inset(-1, -1);
		gc.setColor(backgroundColor);
		gc.fillRect((float) (bounds.getX()), (float) (bounds.getY()),
				(float) (bounds.getX() + bounds.getWidth()),
				(float) (bounds.getY() + bounds.getHeight()));

		gc.setColor(textColor);
		gc.drawText(text, x, y);
	}

	private static int getScale(int m) {
		int temp = m;
		int mul = 1;
		while (temp >= 10) {
			temp /= 10;
			mul *= 10;
		}

		if (temp >= 5)
			return 5 * mul;
		else if (temp >= 2)
			return 2 * mul;
		else
			return mul;
	}

	public static void drawScale(GC gc, MapViewParams mapPos) {
		int lineHeight = gc.getHeight() / 24;
		int scaleWidth = 5;

		int m = (int) pt2m(gc.getWidth() / 2, mapPos);
		int m1 = getScale(m);

		gc.setStrokeWidth(1);

		int len = gc.getWidth() / 2 * m1 / m;

		int x = 4;
		int y = gc.getHeight() - scaleWidth * 2;

		gc.setColor(COLOR.BLACK);
		gc.fillRect(x, y, x + len / 4, y + scaleWidth);
		gc.fillRect(x + len / 2, y, x + len * 3 / 4, y + scaleWidth);
		gc.setColor(COLOR.WHITE);
		gc.fillRect(x + len / 4, y, x + len * 2 / 4, y + scaleWidth);
		gc.fillRect(x + len * 3 / 4, y, x + len, y + scaleWidth);
		gc.setColor(COLOR.BLACK);
		gc.drawRect(x, y, x + len, y + scaleWidth);
		gc.setColor(COLOR.WHITE);
		gc.drawRect(x + 1, y + 1, x + len - 1, y + scaleWidth - 1);
		gc.setColor(COLOR.BLACK);

		gc.setTextSize(lineHeight);
		String text = Float.toString((float) m1 / 1000) + "km";
		if ((m1 >= 1000) && (m1 % 1000 == 0))
			text = Integer.toString(m1 / 1000) + "km";
		else
			text = Float.toString((float) m1 / 1000) + "km";

		ViewHelper.drawText(gc, text, x, y - scaleWidth, COLOR.BLACK,
				0x80FFFFFF);
	}

	// public static void drawLineToTarget(GC gc, int x0, int y0, MapViewParams
	// mapPos,
	// LocationX targ) {
	// if (targ == null)
	// return;
	//
	// int targx = (int) mapPos.geo2scrX(targ.getX(mapPos.getZoom()),
	// targ.getY(mapPos.getZoom()))
	// + x0;
	// int targy = (int) mapPos.geo2scrY(targ.getX(mapPos.getZoom()),
	// targ.getY(mapPos.getZoom()))
	// + y0;
	//
	// gc.setColor(COLOR.dimm(COLOR.TARG_COLOR));
	// gc.setStrokeWidth(2);
	// gc.drawLine(x0, y0, targx, targy);
	// }

	public static void drawLine(GC gc, int x0, int y0, MapViewParams mapPos,
			LocationX loc1, LocationX loc2, int color) {

		int x1 = (int) mapPos.geo2scrX(loc1.getX(mapPos.getZoom()),
				loc1.getY(mapPos.getZoom()))
				+ x0;
		int y1 = (int) mapPos.geo2scrY(loc1.getX(mapPos.getZoom()),
				loc1.getY(mapPos.getZoom()))
				+ y0;

		int x2 = (int) mapPos.geo2scrX(loc2.getX(mapPos.getZoom()),
				loc2.getY(mapPos.getZoom()))
				+ x0;
		int y2 = (int) mapPos.geo2scrY(loc2.getX(mapPos.getZoom()),
				loc2.getY(mapPos.getZoom()))
				+ y0;

		// gc.setColor(COLOR.dimm(COLOR.TARG_COLOR));
		gc.setColor(color);
		gc.setStrokeWidth(2);
		gc.drawLine(x1, y1, x2, y2);
	}

	public static void drawCross(GC gc, int x, int y) {
		gc.setColor(0xFF000000);
		gc.setStrokeWidth(2);
		int sz = gc.getWidth() / 16;
		gc.drawLine(x, y - sz, x, y + sz);
		gc.drawLine(x - sz, y, x + sz, y);
	}

	public static void drawMyLocationArrow(GC gc, int x0, int y0,
			MapViewParams mapPos, LocationX loc, boolean dimmed) {
		if (loc == null)
			return;

		int x = (int) mapPos.geo2scrX(loc.getX(mapPos.getZoom()),
				loc.getY(mapPos.getZoom()))
				+ x0;
		int y = (int) mapPos.geo2scrY(loc.getX(mapPos.getZoom()),
				loc.getY(mapPos.getZoom()))
				+ y0;

		double accPt = m2pt(loc.getAccuracy(), mapPos);

		if (accPt > 10) {
			gc.setColor(COLOR.MAGENTA & 0x80FFFFFF);
			gc.fillCircle(x, y, (float) accPt);
		}

		gc.drawArrow(x, y, loc.getBearing() + mapPos.angle(), dimmed);
	}

	private static double pt2m(int pt, MapViewParams mapPos) {
		double m1deg = 111000;
		double lat1 = Utils.y2lat(mapPos.centerY() - pt / 2, mapPos.getZoom());
		double lat2 = Utils.y2lat(mapPos.centerY() + pt / 2, mapPos.getZoom());
		return (lat1 - lat2) * m1deg;
	}

	private static double m2pt(double m, MapViewParams mapPos) {
		double m1deg = 111000;
		double lat1 = Utils.y2lat(mapPos.centerY() - 10 / 2, mapPos.getZoom());
		double lat2 = Utils.y2lat(mapPos.centerY() + 10 / 2, mapPos.getZoom());
		return m * 10 / ((lat1 - lat2) * m1deg);
	}

}
