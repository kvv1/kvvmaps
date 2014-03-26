package kvv.kvvmap.tiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.util.Cache;
import kvv.kvvmap.util.Recycleable;

public abstract class Tiles implements Recycleable {

	private Cache<Long, Tile> tileCache;
	private final Adapter adapter;
	private final TileLoader loader;

	protected abstract void onTileLoaded(Tile tile);

	// private static void log(String s) {}

	private int cacheSize;
	private int tileSize;

	private void checkCacheSize() {
		if (tileCache == null || cacheSize != Adapter.MAP_TILES_CACHE_SIZE
				|| tileSize != Adapter.TILE_SIZE) {
			if (tileCache != null)
				tileCache.clear();
			cacheSize = Adapter.MAP_TILES_CACHE_SIZE;
			tileSize = Adapter.TILE_SIZE;
			tileCache = new Cache<Long, Tile>(Adapter.MAP_TILES_CACHE_SIZE) {
				@Override
				protected void dispose(Tile tile) {
					tile.dispose();
				};
			};
		}
	}

	public void putTile(Tile tile) {
		adapter.assertUIThread();
		checkCacheSize();

		if (adapter.getBitmapWidth(tile.img.img) != Adapter.TILE_SIZE) {
			Adapter.log("BITMAP SIZE");
			return;
		}

		adapter.addRecycleable(Tiles.this);
		// System.out.println(tile.id);
		tileCache.remove(tile.id);
		tileCache.put(tile.id, tile);
		Tiles.this.onTileLoaded(tile);
	}

	public Tiles(Adapter adapter, TileLoader loader) {
		this.adapter = adapter;
		this.loader = loader;
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
		for (long id : tileCache.keySet())
			tileCache.get(id).expired = true;
	}

	public Tile getTile(long id, int centerX, int centerY,
			boolean startLoadingIfNeeded) {
		adapter.assertUIThread();
		checkCacheSize();
		Tile tile = tileCache.get(id);

		if ((tile == null || tile.expired) && startLoadingIfNeeded)
			loader.load(id, centerX, centerY);

		return tile;
	}

	public void cancelLoading() {
		loader.cancelLoading();
	}

}
