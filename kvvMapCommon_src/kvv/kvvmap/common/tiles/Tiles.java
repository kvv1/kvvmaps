package kvv.kvvmap.common.tiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.RectInt;
import kvv.kvvmap.common.Cache;
import kvv.kvvmap.common.Recycleable;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.tiles.TileLoader.TileSource;

public abstract class Tiles implements Recycleable {

	private Cache<Long, Tile> tileCache;
	private final Adapter adapter;
	private final TileLoader loader;

	protected abstract void loaded(Tile tile);

	// private static void log(String s) {}

	private int cacheSize;

	private void checkCacheSize() {
		if (tileCache == null || cacheSize != Adapter.MAP_TILES_CACHE_SIZE) {
			if (tileCache != null)
				tileCache.clear();
			cacheSize = Adapter.MAP_TILES_CACHE_SIZE;
			tileCache = new Cache<Long, Tile>(Adapter.MAP_TILES_CACHE_SIZE) {
				@Override
				protected void dispose(Tile tile) {
					tile.dispose();
				};
			};
		}
	}

	private final TileLoaderCallback callback = new TileLoaderCallback() {
		@Override
		public void loaded(Tile tile) {
			adapter.assertUIThread();
			adapter.addRecycleable(Tiles.this);
			// System.out.println(tile.id);
			checkCacheSize();
			tileCache.remove(tile.id);
			tileCache.put(tile.id, tile);
			Tiles.this.loaded(tile);
		}
	};

	public Tiles(Adapter adapter, TileSource tileSource) {
		this.adapter = adapter;
		this.loader = new TileLoader(adapter, tileSource);
	}

	@Override
	public void recycle() {
		adapter.assertUIThread();
		checkCacheSize();
		tileCache.clear();
	}

	public void setInvalid(long id) {
		adapter.assertUIThread();
		checkCacheSize();
		Tile tile = tileCache.get(id);
		if (tile != null)
			tile.expired = true;
	}

	public void setInvalidAll() {
		adapter.assertUIThread();
		checkCacheSize();
		for (long id : tileCache.keySet()) {
			tileCache.get(id).expired = true;
		}
	}

	public Tile getTile(long id, int centerX, int centerY,
			boolean startLoadingIfNeeded) {
		adapter.assertUIThread();
		checkCacheSize();
		Tile tile = tileCache.get(id);
		if (tile != null) {
			if (tile.expired && startLoadingIfNeeded) {
				loader.load(id, callback, centerX, centerY);
			}
			return tile;
		}
		if (startLoadingIfNeeded)
			loader.load(id, callback, centerX, centerY);
		return null;
	}

	private final RectInt src = new RectInt();
	private final RectInt dst = new RectInt();

	public void drawTile(GC gc, int centerX, int centerY, long id, int x,
			int y, boolean loadIfNeeded, int zoom, int prevZoom) {
		adapter.assertUIThread();

		int _sz = Adapter.TILE_SIZE;
		int _x = 0;
		int _y = 0;
		int _nx = TileId.nx(id);
		int _ny = TileId.ny(id);
		int _z = TileId.zoom(id);

		Tile tile = getTile(id, centerX, centerY, loadIfNeeded);
		if (tile != null) {
			src.set(_x, _y, _sz, _sz);
			dst.set(x, y, Adapter.TILE_SIZE, Adapter.TILE_SIZE);
			tile.draw(gc, src, dst);
			return;
		}

		if (zoom > prevZoom) {
			while (_z > Utils.MIN_ZOOM) {

				if (_sz >= 2) {
					_x = _x / 2 + ((_nx & 1) * Adapter.TILE_SIZE / 2);
					_y = _y / 2 + ((_ny & 1) * Adapter.TILE_SIZE / 2);
					_sz /= 2;
				}

				_nx /= 2;
				_ny /= 2;
				_z--;

				tile = getTile(TileId.make(_nx, _ny, _z), centerX, centerY,
						false);
				if (tile != null) {
					src.set(_x, _y, _sz, _sz);
					dst.set(x, y, Adapter.TILE_SIZE, Adapter.TILE_SIZE);
					tile.draw(gc, src, dst);
					return;
				}
			}
		}
		if (zoom < prevZoom && _z < Utils.MAX_ZOOM) {
			src.set(0, 0, Adapter.TILE_SIZE, Adapter.TILE_SIZE);

			tile = getTile(TileId.make(_nx * 2, _ny * 2, _z + 1), centerX,
					centerY, false);
			if (tile != null) {
				dst.set(x, y, Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = getTile(TileId.make(_nx * 2 + 1, _ny * 2, _z + 1), centerX,
					centerY, false);
			if (tile != null) {
				dst.set(x + Adapter.TILE_SIZE / 2, y, Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = getTile(TileId.make(_nx * 2, _ny * 2 + 1, _z + 1), centerX,
					centerY, false);
			if (tile != null) {
				dst.set(x, y + Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = getTile(TileId.make(_nx * 2 + 1, _ny * 2 + 1, _z + 1),
					centerX, centerY, false);
			if (tile != null) {
				dst.set(x + Adapter.TILE_SIZE / 2, y + Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
		}
	}

	public void cancelLoading() {
		loader.cancelLoading();
	}

}
