package kvv.kvvmap.common.maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.CopyOnWriteArrayList;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.Cache;
import kvv.kvvmap.common.Img;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileContent;
import kvv.kvvmap.common.tiles.TileLoader;
import kvv.kvvmap.common.tiles.TileLoader.TileLoaderCallback;

public class Maps {

	private final Adapter adapter;
	private final TileLoader tileLoader;
	private final CopyOnWriteArrayList<MapDescr> maps = new CopyOnWriteArrayList<MapDescr>();

	static class CacheKey {
		public CacheKey(MapDescr mapDescr, int idx) {
			this.mapDescr = mapDescr;
			this.idx = idx;
		}

		private final MapDescr mapDescr;
		private final int idx;

		@Override
		public int hashCode() {
			return idx;
		}

		@Override
		public boolean equals(Object obj) {
			CacheKey key = (CacheKey) obj;
			return mapDescr == key.mapDescr && idx == key.idx;
		}
	}

	public Maps(final Adapter adapter, MapsDir mapsDir) {
		this.adapter = adapter;

		tileLoader = new TileLoader(adapter) {
			@Override
			protected Tile loadAsync(long id) {
				TileContent content = new TileContent();
				Img img = MapDescr.load(maps, id, 0, 0, Utils.TILE_SIZE_G, null,
						content);
				if (img == null)
					return null;
				return new Tile(adapter, id, img, content);
			}
		};

		Cache<CacheKey, byte[]> cache = new Cache<CacheKey, byte[]>(
				Adapter.RAF_CACHE_SIZE);

		for (String name : mapsDir.names()) {
			try {
				maps.add(new MapDescr(cache, new File(Adapter.MAPS_ROOT, name
						+ ".pac"), adapter, mapsDir.get(name)));
				Adapter.log("map " + name + " loaded");
				Adapter.logMem();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void load(Long id, final TileLoaderCallback callback, PointInt centerXY) {
		adapter.assertUIThread();
		tileLoader.load(id, callback, centerXY);
	}

	public void cancelLoading() {
		tileLoader.cancelLoading();
	}

	public void reorder(String map) {
		for(MapDescr md : maps) {
			if(md.getName().equals(map)) {
				maps.remove(md);
				maps.add(0, md);
				return;
			}
		}
	}

	public String getTopMap() {
		return maps.get(0).getName();
	}

	public void setTopMap(String map) {
		for (MapDescr md : maps)
			if (md.getName().equals(map)) {
				maps.remove(md);
				maps.add(0, md);
				return;
			}
	}

	public void dispose() {
		//maps.clear();
		tileLoader.dispose();
	}
	
	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Maps");
		super.finalize();
	}

}
