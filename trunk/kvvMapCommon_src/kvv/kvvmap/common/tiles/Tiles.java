package kvv.kvvmap.common.tiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.adapter.RectInt;
import kvv.kvvmap.common.Cache;
import kvv.kvvmap.common.Recycleable;
import kvv.kvvmap.common.Utils;

public abstract class Tiles implements Recycleable {

	private final Cache<Long, Tile> tileCache;
	private final Adapter adapter;

	// private final Set<Long> tilesToIgnore = new HashSet<Long>();

	protected abstract void load(Long id, final TileLoaderCallback callback,
			PointInt prioLoc);

	protected abstract void loaded(Tile tile);

	// private static void log(String s) {}

	private final TileLoaderCallback callback = new TileLoaderCallback() {
		@Override
		public void loaded(Tile tile) {
			adapter.assertUIThread();
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
	public void recycle() {
		adapter.assertUIThread();
		tileCache.clear();
	}

	public void setInvalid(long id) {
		adapter.assertUIThread();
		Tile tile = tileCache.get(id);
		if (tile != null)
			tile.needsReloading = true;
	}

	public void setInvalidAll() {
		adapter.assertUIThread();
		for (long id : tileCache.keySet()) {
			tileCache.get(id).needsReloading = true;
		}
	}

	public Tile getTile(long id, PointInt prioLoc, boolean startLoadingIfNeeded) {
		adapter.assertUIThread();
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

	private final RectInt src = new RectInt();
	private final RectInt dst = new RectInt();

	public void drawTile(GC gc, PointInt centerXY, long id, int x, int y,
			boolean loadIfNeeded, int zoom, int prevZoom) {
		adapter.assertUIThread();

		int _sz = Adapter.TILE_SIZE;
		int _x = 0;
		int _y = 0;
		int _nx = TileId.nx(id);
		int _ny = TileId.ny(id);
		int _z = TileId.zoom(id);

		Tile tile = getTile(id, centerXY, loadIfNeeded);
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

				tile = getTile(TileId.make(_nx, _ny, _z), centerXY, false);
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

			tile = getTile(TileId.make(_nx * 2, _ny * 2, _z + 1), centerXY,
					false);
			if (tile != null) {
				dst.set(x, y, Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = getTile(TileId.make(_nx * 2 + 1, _ny * 2, _z + 1), centerXY,
					false);
			if (tile != null) {
				dst.set(x + Adapter.TILE_SIZE / 2, y, Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = getTile(TileId.make(_nx * 2, _ny * 2 + 1, _z + 1), centerXY,
					false);
			if (tile != null) {
				dst.set(x, y + Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = getTile(TileId.make(_nx * 2 + 1, _ny * 2 + 1, _z + 1),
					centerXY, false);
			if (tile != null) {
				dst.set(x + Adapter.TILE_SIZE / 2, y + Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
		}
	}

}
