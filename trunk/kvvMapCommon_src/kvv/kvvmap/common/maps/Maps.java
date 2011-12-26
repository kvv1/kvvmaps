package kvv.kvvmap.common.maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.CopyOnWriteArrayList;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.Cache;
import kvv.kvvmap.common.Img;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileContent;
import kvv.kvvmap.common.tiles.TileId;
import kvv.kvvmap.common.tiles.TileLoader;
import kvv.kvvmap.common.tiles.TileLoaderCallback;

public class Maps {

	private final Adapter adapter;
	private final TileLoader tileLoader;
	private final CopyOnWriteArrayList<MapDescrBase> maps = new CopyOnWriteArrayList<MapDescrBase>();

	private volatile MapDescrBase fixedMap;

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
				Img img = MapDescr.load(maps, fixedMap, TileId.nx(id),
						TileId.ny(id), TileId.zoom(id), content);
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

	public void load(Long id, final TileLoaderCallback callback,
			PointInt centerXY) {
		adapter.assertUIThread();
		tileLoader.load(id, callback, centerXY);
	}

	public void cancelLoading() {
		tileLoader.cancelLoading();
	}

	public void reorder(String map) {
		for (MapDescrBase md : maps) {
			if (md.getName().equals(map)) {
				maps.remove(md);
				maps.add(0, md);
				return;
			}
		}
	}

	public void setTopMap(String map) {
		for (MapDescrBase md : maps)
			if (md.getName().equals(map)) {
				maps.remove(md);
				maps.add(0, md);
				return;
			}
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Maps");
		super.finalize();
	}

	public void fixMap(String map) {
		if(map == null) {
			fixedMap = null;
		} else {
			for (MapDescrBase md : maps)
				if (md.getName().equals(map)) {
					fixedMap = md;
					return;
				}
		}
		Adapter.log("fixed map = "
				+ (fixedMap != null ? fixedMap.getName() : null));
	}
}
