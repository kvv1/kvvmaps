package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.Utils;

public class ViewHelper {
	public static void drawText(GC gc, String text, PointInt pt, int textColor,
			int backgroundColor) {
		RectX bounds = gc.getTextBounds(text);
		bounds.offset(pt.x, pt.y);
		bounds.inset(-1, -1);
		gc.setColor(backgroundColor);
		gc.fillRect((float) (bounds.getX()), (float) (bounds.getY()),
				(float) (bounds.getX() + bounds.getWidth()),
				(float) (bounds.getY() + bounds.getHeight()));

		gc.setColor(textColor);
		gc.drawText(text, pt.x, pt.y);
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

		int x0 = 4;
		int x = x0;
		int y = lineHeight + scaleWidth + scaleWidth;

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

		ViewHelper.drawText(gc, text, new PointInt(4, lineHeight), COLOR.BLACK,
				0x80FFFFFF);
	}

	public static void drawTarget(GC gc, MapViewParams mapPos,
			LocationX center, LocationX myLoc, LocationX targ) {
		if (targ == null)
			return;

		int _dx = (int) Math.abs(mapPos.geo2scrX(targ.getX(mapPos.getZoom()), targ.getY(mapPos.getZoom())));
		int _dy = (int) Math.abs(mapPos.geo2scrY(targ.getX(mapPos.getZoom()), targ.getY(mapPos.getZoom())));
		if (_dx > gc.getWidth() / 3 || _dy > gc.getHeight() / 3) {
			gc.setColor(COLOR.TARG_COLOR);
			gc.setStrokeWidth(2);

			int len = gc.getWidth() / 16;

			double bearing = (90 - center.bearingTo(targ)) * Math.PI / 180;

			int dx = (int) (len * Math.cos(bearing));
			int dy = (int) (len * Math.sin(bearing));

			int x = gc.getWidth() / 2;
			int y = gc.getHeight() / 2;

			gc.drawLine(x, y, x + dx, y - dy);

		}

		if (myLoc != null) {
			String txt = Utils.formatDistance((int) myLoc.distanceTo(targ));
			gc.setTextSize(gc.getHeight() / 24);

			RectX txtBounds = gc.getTextBounds(txt);

			PointInt pt = new PointInt((int) (gc.getWidth() - gc.getWidth()
					/ 10 - txtBounds.getWidth() / 2),
					(int) (gc.getHeight() / 5 + txtBounds.getHeight()));

			ViewHelper.drawText(gc, txt, pt, COLOR.WHITE, COLOR.TARG_COLOR);
		}
	}

	public static void drawCross(GC gc) {
		gc.setColor(0xFF000000);
		gc.setStrokeWidth(2);
		int x = gc.getWidth() / 2;
		int y = gc.getHeight() / 2;
		int sz = gc.getWidth() / 16;
		gc.drawLine(x, y - sz, x, y + sz);
		gc.drawLine(x - sz, y, x + sz, y);
	}

	public static int drawMyLocation(GC gc, MapViewParams mapPos,
			LocationX loc, boolean dimmed) {
		if (loc == null)
			return 0;

		int x = (int) mapPos.geo2scrX(loc.getX(mapPos.getZoom()), loc.getY(mapPos.getZoom())) + gc.getWidth() / 2;
		int y = (int) mapPos.geo2scrY(loc.getX(mapPos.getZoom()), loc.getY(mapPos.getZoom())) + gc.getHeight() / 2;

		double accPt = m2pt(loc.getAccuracy(), mapPos);

		if (accPt > 10) {
			gc.setColor(COLOR.MAGENTA & 0x80FFFFFF);
			gc.fillCircle(x, y, (float) accPt);
		}

		gc.drawArrow(x, y, loc, dimmed);

		int lineHeight = gc.getHeight() / 24;
		gc.setTextSize(lineHeight);

		gc.setColor(0x80000000);
		gc.fillRect(0, gc.getHeight() - lineHeight, gc.getWidth(),
				gc.getHeight());

		gc.setColor(COLOR.CYAN);
		gc.drawText(
				Utils.format(loc.getLongitude()) + " "
						+ Utils.format(loc.getLatitude()) + " "
						+ (int) loc.getAltitude() + "m "
						+ (int) (loc.getSpeed() * 3.6f) + "km/h", 0,
				gc.getHeight() - 2);

		return lineHeight;
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
