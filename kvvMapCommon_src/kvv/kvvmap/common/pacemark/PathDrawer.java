package kvv.kvvmap.common.pacemark;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.tiles.TileId;

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
		// gc.drawText(text, x+1, y+1);
		// gc.setColor(COLOR.BLUE);
		gc.drawText(text, x, y);
	}

	private static RectX rect = new RectX(0, 0, 0, 0);

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
		synchronized (rect) {
			rect.set(lon, lat, lonw, lath);
			for (Path path : paths.getPaths()) {
				if (path.filter(rect))
					drawPath(path, gc, tileId, infoLevel, sel);
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
		gc.setColor(COLOR.RED);

		if (selPM != null)
			gc.setStrokeWidth(4);
		else
			gc.setStrokeWidth(2);

		int nx = TileId.nx(id);
		int ny = TileId.ny(id);
//		int z = TileId.zoom(id);

		int dx = nx * Adapter.TILE_SIZE;
		int dy = ny * Adapter.TILE_SIZE;

		int[] pts = pathInZoom.getPoints(id);
		for (int i = 0; i < pts.length - 2; i += 2) {
			gc.drawLine(pts[i] - dx, pts[i + 1] - dy, pts[i + 2] - dx,
					pts[i + 3] - dy);
		}
	}

	public static void drawPlacemarks(PlaceMarks pms, GC gc, long id, InfoLevel infoLevel, ISelectable sel) {
		int dx = TileId.nx(id) * Adapter.TILE_SIZE;
		int dy = TileId.ny(id) * Adapter.TILE_SIZE;
		for (LocationX pm : pms.getPlaceMarks()) {
			int x = pm.getX(TileId.zoom(id)) - dx;
			int y = pm.getY(TileId.zoom(id)) - dy;

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

}
