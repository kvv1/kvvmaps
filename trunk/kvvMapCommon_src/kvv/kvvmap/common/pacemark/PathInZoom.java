package kvv.kvvmap.common.pacemark;

import java.util.ArrayList;
import java.util.List;

import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.IntArray;

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

		double x1 = pm1.getXd(zoom) - pm.getXd(zoom);
		double y1 = pm1.getYd(zoom) - pm.getYd(zoom);
		double x2 = pm2.getXd(zoom) - pm.getXd(zoom);
		double y2 = pm2.getYd(zoom) - pm.getYd(zoom);

		if (x1 * x2 + y1 * y2 > 0)
			return true;

		double s = Math.abs(x1 * y2 - x2 * y1);
		return s > sq;
	}


	public synchronized int[] getPoints(long id) {
		IntArray indices = get(id);
		if (indices == null)
			return new int[0];

		if (indices.size() == 0)
			return new int[0];

		int[] res = new int[indices.size() * 2 + 2];

		res[0] = placemarks.get(indices.get(0) - 1).getX(zoom);
		res[1] = placemarks.get(indices.get(0) - 1).getY(zoom);

		for (int i = 0; i < indices.size(); i++) {
			res[i * 2 + 2] = placemarks.get(indices.get(i)).getX(zoom);
			res[i * 2 + 2 + 1] = placemarks.get(indices.get(i)).getY(zoom);
		}

		return res;
	}

	public synchronized List<LocationX> getPlaceMarks() {
		return new ArrayList<LocationX>(placemarks);
	}
}