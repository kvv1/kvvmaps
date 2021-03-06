package kvv.kvvmap.maps;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.maps.MapsDir.MapsDirListener;

public class Maps {

	public static interface MapsListener {
		void mapsChanged();
	}

	private MapsListener listener;

	private final Adapter adapter;
	public final List<MapDescrBase> maps = new CopyOnWriteArrayList<MapDescrBase>();

	public volatile MapDescrBase fixedMap;

	public Maps(final Adapter adapter, final MapsDir mapsDir) {
		this.adapter = adapter;
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

		//maps.add(new GoogleMapDescr("GOOGLE"));

	}

	public void addMap(String name, MapsDir mapsDir) {
		adapter.assertUIThread();
		try {
			MapDir[] mapDir = mapsDir.get(name);
			maps.add(new MapDescr(name, adapter, mapDir));
			if (listener != null)
				listener.mapsChanged();
			//Adapter.log("map " + name + " loaded");
			//Adapter.logMem();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void setListener(MapsListener l) {
		listener = l;
	}

	public void reorder(String map) {
		for (MapDescrBase md : maps) {
			if (md.getName().equals(map)) {
				maps.remove(md);
				maps.add(0, md);
				if (listener != null)
					listener.mapsChanged();
				return;
			}
		}
	}

	public void setTopMap(String map) {
		for (MapDescrBase md : maps)
			if (md.getName().equals(map)) {
				maps.remove(md);
				maps.add(0, md);
				if (listener != null)
					listener.mapsChanged();
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
			if (listener != null)
				listener.mapsChanged();
		} else {
			for (MapDescrBase md : maps)
				if (md.getName().equals(map)) {
					fixedMap = md;
					if (listener != null)
						listener.mapsChanged();
					return;
				}
		}
	}
/*
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
	*/

	public Tile loadAsync(long tileId) {
		return MapDescrBase.loadAsync(maps, fixedMap, tileId, adapter);
	}
}
