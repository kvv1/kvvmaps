package kvv.kvvmap.common.pacemark;

import java.io.File;
import java.util.List;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.tiles.TileId;

public class PathDrawer {

	public static void drawLabel(GC gc, String text, int x, int y) {
		gc.setTextSize(16);

		RectX rect = gc.getTextBounds(text);
		rect.offset(x, y);
		rect.inset(-2, -2);

		gc.setColor(0x80000000);
		gc.fillRect((int) rect.getX(), (int) rect.getY(),
				(int) (rect.getX() + rect.getWidth()),
				(int) (rect.getY() + rect.getHeight()));
		gc.setColor(COLOR.CYAN);
		// gc.drawText(text, x+1, y+1);
		// gc.setColor(COLOR.BLUE);
		gc.drawText(text, x, y);
	}

	private static RectX __rect = new RectX(0, 0, 0, 0);

	public static void drawPaths(Paths paths, GC gc, long tileId,
			InfoLevel infoLevel, ISelectable sel) {
		int x0 = TileId.nx(tileId) * Adapter.TILE_SIZE;
		int x1 = (TileId.nx(tileId) + 1) * Adapter.TILE_SIZE;
		int y0 = TileId.ny(tileId) * Adapter.TILE_SIZE;
		int y1 = (TileId.ny(tileId) + 1) * Adapter.TILE_SIZE;
		int z = TileId.zoom(tileId);
		double lon = Utils.x2lon(x0, z);
		double lonw = Utils.x2lon(x1, z) - lon;
		double lat = Utils.y2lat(y1, z);
		double lath = Utils.y2lat(y0, z) - lat;

		Path selectedPath = null;
		if (sel instanceof PathSelection)
			selectedPath = ((PathSelection) sel).path;

		synchronized (__rect) {
			__rect.set(lon, lat, lonw, lath);
			for (Path path : paths.getPaths()) {
				if (path != selectedPath)
					if (path.filter(__rect))
						drawPath(path, gc, tileId, infoLevel, sel);
			}
			if (selectedPath != null) {
				if (selectedPath.filter(__rect))
					drawPath(selectedPath, gc, tileId, infoLevel, sel);
			}
		}
	}

	public static void drawPath(Path path, GC gc, long id, InfoLevel infoLevel,
			ISelectable sel) {
		if (!path.isEnabled())
			return;
		LocationX selPM = null;
		if (sel instanceof PathSelection) {
			PathSelection pathSel = (PathSelection) sel;
			if (pathSel.path == path)
				selPM = pathSel.pm;
		}
		drawPathInZoom(path.pathsInZooms[TileId.zoom(id)], gc, id, infoLevel,
				selPM);
	}

	public static void drawPathInZoom(PathInZoom pathInZoom, GC gc, long id,
			InfoLevel infoLevel, LocationX selPM) {

		if (selPM != null) {
			drawPathInZoom(pathInZoom, gc, id, infoLevel, 0x7fFFFFFF, 8);
			drawPathInZoom(pathInZoom, gc, id, infoLevel, COLOR.ALYI, 6);
			drawPathInZoom(pathInZoom, gc, id, infoLevel, COLOR.YELLOW, 2);
		} else {
			drawPathInZoom(pathInZoom, gc, id, infoLevel, 0x7fFFFFFF, 8);
			drawPathInZoom(pathInZoom, gc, id, infoLevel, COLOR.ALYI, 4);
			drawPathInZoom(pathInZoom, gc, id, infoLevel, COLOR.YELLOW, 1);
		}
	}

	public static void drawPathInZoom(PathInZoom pathInZoom, GC gc, long id,
			InfoLevel infoLevel, int color, int width) {
		gc.setColor(color);
		gc.setStrokeWidth(width);

		int nx = TileId.nx(id);
		int ny = TileId.ny(id);
		// int z = TileId.zoom(id);

		int dx = nx * Adapter.TILE_SIZE;
		int dy = ny * Adapter.TILE_SIZE;

		int[] pts = pathInZoom.getPoints(id);
		for (int i = 0; i < pts.length; i += 4) {
			gc.drawLine(pts[i] - dx, pts[i + 1] - dy, pts[i + 2] - dx,
					pts[i + 3] - dy);
		}
	}

	public static void drawPlacemarks(PlaceMarks pms, GC gc, long id,
			InfoLevel infoLevel, ISelectable sel) {
		int dx = TileId.nx(id) * Adapter.TILE_SIZE;
		int dy = TileId.ny(id) * Adapter.TILE_SIZE;
		for (LocationX pm : pms.getPlaceMarks()) {
			int x = pm.getXint(TileId.zoom(id)) - dx;
			int y = pm.getYint(TileId.zoom(id)) - dy;

			if (x < -Adapter.TILE_SIZE || x > 2 * Adapter.TILE_SIZE
					|| y < -Adapter.TILE_SIZE || y > 2 * Adapter.TILE_SIZE)
				continue;

			int diam;

			if (pm == sel) {
				gc.setStrokeWidth(2);
				diam = 5;
			} else {
				gc.setStrokeWidth(1);
				diam = 3;
			}

			if (pm == pms.getTarget())
				gc.setColor(COLOR.TARG_COLOR);
			else
				gc.setColor(COLOR.RED);

			gc.drawLine(x, y, x + 5, y - 15);
			gc.fillCircle(x + 5, y - 15, diam);

			String name = pm.name;
			if (infoLevel.ordinal() > InfoLevel.MEDIUM.ordinal()
					&& name != null) {
				PathDrawer.drawLabel(gc, name, x + 5, y + 5);
			}
		}
	}

	public static void drawDiagram(GC gc, PathSelection sel) {
		int y = gc.getHeight();
		int w = gc.getWidth();

		int lineHeight = gc.getHeight() / 6;

		gc.setTextSize(lineHeight);

		gc.setColor(0x80000000);
		gc.fillRect(0, 0, w, y);

		gc.setStrokeWidth(2);

		gc.setColor(COLOR.CYAN);
		gc.drawText(
				Utils.format(sel.pm.getLongitude()) + "  "
						+ Utils.format(sel.pm.getLatitude()) + "  "
						+ (int) sel.pm.getAltitude() + "m "
						+ (int) (sel.pm.getSpeed() * 3.6) + "km/h", 2, y - 2);

		int len = Math.max(1, (int) sel.path.getLen());

		gc.setColor(COLOR.GREEN);

		List<LocationX> pms = sel.path.getPlaceMarks();

		if (pms.size() > 0) {
			int minAlt = (int) pms.iterator().next().getAltitude();
			int maxAlt = (int) pms.iterator().next().getAltitude();

			int minSpeed = -1;
			int maxSpeed = -1;

			for (LocationX pm : pms) {
				float speed = pm.getSpeed();
				minAlt = Math.min(minAlt, (int) pm.getAltitude());
				maxAlt = Math.max(maxAlt, (int) pm.getAltitude());
				if (speed > 0) {
					if(minSpeed >= 0) {
						minSpeed = Math.min(minSpeed, (int) speed);
						maxSpeed = Math.max(maxSpeed, (int) speed);
					} else {
						minSpeed = maxSpeed = (int) speed;
					}
				}
			}

			int altDif = Math.max(1, maxAlt - minAlt);
			int speedDif = Math.max(1, maxSpeed - minSpeed);

			LocationX prevPm = null;
			PointInt prevPt = null;
			PointInt prevPt1 = null;

			float len0 = 0;

			PointInt pmPt = null;

			int rectX = w * 2 / 6;
			int rectY = y - 5 * lineHeight;
			int rectW = w * 4 / 6;
			int rectH = 4 * lineHeight;
			
			for (LocationX pm : pms) {
				if (prevPm != null)
					len0 += pm.distanceTo(prevPm);

				int _x = (int) (rectX + (len0 * rectW / len));
				int _y = (int) (rectY + rectH - (int) ((pm
						.getAltitude() - minAlt) * rectH / altDif));
				int _y1 = (int) (rectY + rectH - (int) ((pm
						.getSpeed() - minSpeed) * rectH / speedDif));

				PointInt pt = new PointInt(_x, _y);
				PointInt pt1 = new PointInt(_x, _y1);

				if (pm == sel.pm)
					pmPt = pt;

				if (prevPm != null) {
					gc.setColor(COLOR.GREEN);
					gc.drawLine(pt.x, pt.y, prevPt.x, prevPt.y);

					if (pm.getSpeed() < 127) {
						if (prevPt1 != null) {
							gc.setColor(COLOR.YELLOW);
							gc.drawLine(pt1.x, pt1.y, prevPt1.x, prevPt1.y);
						}
						prevPt1 = pt1;
					}
				}

				prevPm = pm;
				prevPt = pt;

			}

			gc.setColor(COLOR.CYAN);

			if (pmPt != null) {
				gc.fillCircle(pmPt.x, pmPt.y, 5);
			}

			gc.drawText("" + maxAlt + "m", 2,
					rectY + lineHeight - 1);
			gc.drawText("" + minAlt + "m", 2, rectY + lineHeight
					* 4 - 2);

			gc.drawText("" + len + "m", 2,
					rectY + lineHeight * 2 - 2);
			gc.drawText("" + pms.size(), 2,
					rectY + lineHeight * 3 - 2);
		}

		y -= 5 * lineHeight;

		File file = sel.path.getFile();

		if (file != null) {
			gc.setColor(0x80000000);
			gc.setColor(COLOR.CYAN);
			gc.drawText(file.getName(), 2, y - 2);
			y -= lineHeight;
		}
	}

}
