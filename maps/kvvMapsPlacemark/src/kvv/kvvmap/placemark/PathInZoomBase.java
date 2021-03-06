package kvv.kvvmap.placemark;

import java.util.ArrayList;
import java.util.List;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.util.IntArray;
import kvv.kvvmap.util.LongHashMap;
import kvv.kvvmap.util.TileId;

public class PathInZoomBase {

	protected final int zoom;
	protected final List<LocationX> placemarks = new ArrayList<LocationX>();
	private TileMap tiles = new TileMap();

	private IPlaceMarksListener doc;

	protected PathInZoomBase(int zoom) {
		this.zoom = zoom;
	}

	class TileMap {
		private final LongHashMap<IntArray> tiles = new LongHashMap<IntArray>();

		public void clear() {
			tiles.clear();
			if (doc != null)
				doc.onPathTilesChanged();
		}

		public void add(long id, int idx) {
			IntArray arr = tiles.get(id);
			if (arr == null) {
				arr = new IntArray();
				tiles.put(id, arr);
			}
			arr.add(idx);
			if (doc != null)
				doc.onPathTileChanged(id);
		}

		public void removeValue(int idx) {
			for (long id : tiles.keys()) {
				boolean removed = tiles.get(id).removeValue(idx);
				if (removed && doc != null)
					doc.onPathTileChanged(id);
			}
		}

		public IntArray get(long id) {
			return tiles.get(id);
		}

		public synchronized void add(LocationX lastPm, LocationX pm, int idx) {
			int nx1 = pm.getXint(zoom) / Adapter.TILE_SIZE;
			int ny1 = pm.getYint(zoom) / Adapter.TILE_SIZE;

			int nx2 = lastPm.getXint(zoom) / Adapter.TILE_SIZE;
			int ny2 = lastPm.getYint(zoom) / Adapter.TILE_SIZE;

			if (nx1 > nx2) {
				int t = nx1;
				nx1 = nx2;
				nx2 = t;
			}

			if (ny1 > ny2) {
				int t = ny1;
				ny1 = ny2;
				ny2 = t;
			}

			for (int nx = nx1; nx <= nx2; nx++)
				for (int ny = ny1; ny <= ny2; ny++) {
					add(TileId.make(nx, ny, zoom), idx);
				}
		}

	}

	public synchronized void setDoc(IPlaceMarksListener doc) {
		tiles.clear();
		if (placemarks.size() > 0) {
			LocationX prev = placemarks.get(0);
			for (int i = 1; i < placemarks.size(); i++) {
				LocationX pm = placemarks.get(i);
				tiles.add(prev, pm, i);
				prev = pm;
			}
		}
		this.doc = doc;
	}

	public synchronized int size() {
		return placemarks.size();
	}

	public synchronized void removeAll() {
		placemarks.clear();
		tiles.clear();
	}

	protected synchronized void add(LocationX pm) {
		if (doc != null) {
			int idx = placemarks.size();
			if (idx != 0) {
				LocationX lastPm = placemarks.get(idx - 1);
				tiles.add(lastPm, pm, idx);
			}
		}
		placemarks.add(pm);
	}

	protected IntArray get(long id) {
		return tiles.get(id);
	}

	protected LocationX getLast() {
		return placemarks.get(placemarks.size() - 1);
	}

	protected LocationX removeLast() {
		int idx = placemarks.size() - 1;
		if (idx < 0)
			return null;
		tiles.removeValue(idx);
		return placemarks.remove(idx);
	}
}
