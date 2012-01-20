package kvv.kvvmap.common.maps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.Cache;
import kvv.kvvmap.common.Img;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.maps.MapsDir.MapsDirListener;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileContent;
import kvv.kvvmap.common.tiles.TileId;
import kvv.kvvmap.common.tiles.TileLoader;
import kvv.kvvmap.common.tiles.TileLoaderCallback;

public class Maps {

	public static interface MapsListener {
		void mapAdded(String name);
	}

	private MapsListener listener;

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

	private final Cache<CacheKey, byte[]> cache = new Cache<CacheKey, byte[]>(
			Adapter.RAF_CACHE_SIZE);

	public Maps(final Adapter adapter, final MapsDir mapsDir) {
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

		mapsDir.setListener(new MapsDirListener() {
			@Override
			public void mapAdded(String name) {
				addMap(name, mapsDir);
			}
		});

		Set<String> names = mapsDir.names();
		for (String name : names) {
			addMap(name, mapsDir);
		}

		maps.add(new GoogleMapDescr("GOOGLE"));

	}

	public void addMap(String name, MapsDir mapsDir) {
		adapter.assertUIThread();
		synchronized (cache) {
			cache.clear();
		}
		try {
			MapDir[] mapDir = mapsDir.get(name);
			maps.add(new MapDescr(name, cache, adapter, mapDir));
			if (listener != null)
				listener.mapAdded(name);
			Adapter.log("map " + name + " loaded");
			Adapter.logMem();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void setListener(MapsListener l) {
		listener = l;
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
		if (map == null) {
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

	class GoogleMapDescr extends MapDescrBase {

		public GoogleMapDescr(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected boolean hasTile(int nx, int ny, int zoom) {
			return true;
		}

		@Override
		protected Img load(int nx, int ny, int zoom, int x, int y, int sz,
				Img imgBase) {
			if (imgBase != null && !imgBase.transparent)
				return imgBase;
			
			InputStream is;
			try {
				double centerx = Utils.x2lon(nx * 256 + 128, zoom);
				double centery = Utils.y2lat(ny * 256 + 128, zoom);
				
				String req = String.format("http://maps.google.com/staticmap?center=%g,%g&zoom=%d&size=256x256", centery, centerx, zoom);
				
				is = new URL(req).openStream();
//				is = new URL("http://maps.google.com/staticmap?center=40.714728,-73.998672&zoom=14&size=256x256").openStream();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return imgBase;
			} catch (IOException e) {
				e.printStackTrace();
				return imgBase;
			}
			
			Object bm = adapter.decodeBitmap(is);
			
			if (bm == null)
				return imgBase;

			Object img1 = adapter.allocBitmap();
			if (img1 == null)
				return imgBase;
			adapter.drawOver(img1, bm, x, y, sz);
			adapter.disposeBitmap(bm);

			if (imgBase != null) {
				adapter.drawOver(img1, imgBase.img);
				adapter.disposeBitmap(imgBase.img);
			}

			return new Img(img1, false);
		}

	}
}
