package kvv.kvvmap.placemark;

import java.util.ArrayList;
import java.util.List;

import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.util.IntArray;

class PathInZoom extends PathInZoomBase {
	private List<LocationX> notCompacted = new ArrayList<LocationX>();
	private final static int COMPACT_SQUARE = 30;

	public PathInZoom(int zoom) {
		super(zoom);
	}

	public synchronized LocationX addCompacted(LocationX compactedInPrevZoom,
			LocationX pm) {
		if (size() == 0) {
			add(pm);
			return null;
		}

		if (size() > 1)
			removeLast();

		if (compactedInPrevZoom != null) {
			LocationX lastCompactedPm = getLast();
			notCompacted.add(compactedInPrevZoom);
			for (int idx = notCompacted.size() - 2; idx >= 0; idx--) {
				LocationX curPm = notCompacted.get(idx);
				if (needToCompact(curPm, lastCompactedPm, compactedInPrevZoom,
						COMPACT_SQUARE)) {
					notCompacted.clear();
					notCompacted.add(compactedInPrevZoom);
					add(curPm);
					add(pm);
					return curPm;
				}
			}
		}

		add(pm);
		return null;
	}

	public synchronized void finishCompact() {
		if (size() > 0) {
			if (notCompacted.size() > 0) {
				LocationX last = removeLast();
				add(notCompacted.get(notCompacted.size() - 1));
				add(last);
			}
		}
		notCompacted.clear();
	}

	private boolean needToCompact(LocationX pm, LocationX pm1, LocationX pm2,
			int sq) {

		double x1 = pm1.getX(zoom) - pm.getX(zoom);
		double y1 = pm1.getY(zoom) - pm.getY(zoom);
		double x2 = pm2.getX(zoom) - pm.getX(zoom);
		double y2 = pm2.getY(zoom) - pm.getY(zoom);

		if (x1 * x2 + y1 * y2 > 0)
			return true;

		double s = Math.abs(x1 * y2 - x2 * y1);
		return s > sq;
	}

//	public synchronized void draw(GC gc, long id, InfoLevel infoLevel,
//			LocationX selPM) {
//		IntArray indices = get(id);
//		if (indices == null)
//			return;
//
//		if (indices.size() == 0)
//			return;
//
//		gc.setColor(COLOR.RED);
//
//		if (selPM != null)
//			gc.setStrokeWidth(4);
//		else
//			gc.setStrokeWidth(2);
//
//		int nx = TileId.nx(id);
//		int ny = TileId.ny(id);
//		int z = TileId.zoom(id);
//
//		int dx = nx * Adapter.TILE_SIZE;
//		int dy = ny * Adapter.TILE_SIZE;
//
//		for (int i : indices.values()) {
//			int x1 = placemarks.get(i - 1).getX(z);
//			int y1 = placemarks.get(i - 1).getY(z);
//			int x2 = placemarks.get(i).getX(z);
//			int y2 = placemarks.get(i).getY(z);
//			gc.drawLine(x1 - dx, y1 - dy, x2 - dx, y2 - dy);
//		}
//	}

	public synchronized int[] getPoints(long id) {
		IntArray indices = get(id);
		if (indices == null)
			return new int[0];

		if (indices.size() == 0)
			return new int[0];

		int[] res = new int[indices.size() * 4];

		for (int i = 0; i < indices.size(); i++) {
			int ii = indices.get(i);
			
			res[i * 4] = placemarks.get(ii - 1).getXint(zoom);
			res[i * 4 + 1] = placemarks.get(ii - 1).getYint(zoom);
			res[i * 4 + 2] = placemarks.get(ii).getXint(zoom);
			res[i * 4 + 3] = placemarks.get(ii).getYint(zoom);
		}

		return res;
	}

	public synchronized List<LocationX> getPlaceMarks() {
		return new ArrayList<LocationX>(placemarks);
	}
}