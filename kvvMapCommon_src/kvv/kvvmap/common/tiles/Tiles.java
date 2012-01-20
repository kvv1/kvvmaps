package kvv.kvvmap.common.tiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.Cache;
import kvv.kvvmap.common.Recycleable;

public abstract class Tiles implements Recycleable {

	private final Cache<Long, Tile> tileCache;
	private final Adapter adapter;

//	private final Set<Long> tilesToIgnore = new HashSet<Long>();

	protected abstract void load(Long id, final TileLoaderCallback callback,
			PointInt prioLoc);

	protected abstract void loaded(Tile tile);

	// private static void log(String s) {}

	private final TileLoaderCallback callback = new TileLoaderCallback() {
		@Override
		public void loaded(Tile tile) {
			adapter.addRecycleable(Tiles.this);
			// System.out.println(tile.id);
			tileCache.remove(tile.id);
			tileCache.put(tile.id, tile);
			Tiles.this.loaded(tile);
		}
	};

	public Tiles(Adapter adapter, int cacheSize) {
		this.adapter = adapter;
		tileCache = new Cache<Long, Tile>(cacheSize) {
			@Override
			protected void dispose(Tile tile) {
				tile.dispose();
			};
		};
	}

	@Override
	public synchronized void recycle() {
		tileCache.clear();
	}

	public synchronized void setInvalid(long id) {
		Tile tile = tileCache.get(id);
		if (tile != null)
			tile.needsReloading = true;
	}

	public synchronized void setInvalidAll() {
		for (long id : tileCache.keySet()) {
			tileCache.get(id).needsReloading = true;
		}
	}

	public synchronized Tile getTile(long id, PointInt prioLoc,
			boolean startLoadingIfNeeded) {
		Tile tile = tileCache.get(id);
		if (tile != null) {
			if (tile.needsReloading && startLoadingIfNeeded) {
				load(id, callback, prioLoc);
			}
			return tile;
		}
		if (startLoadingIfNeeded)
			load(id, callback, prioLoc);
		return null;
	}

}
