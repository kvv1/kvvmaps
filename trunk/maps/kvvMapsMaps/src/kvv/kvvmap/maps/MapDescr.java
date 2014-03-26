package kvv.kvvmap.maps;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.util.Img;
import kvv.kvvmap.util.PackedDataFile;

public class MapDescr extends MapDescrBase {
	// private final Cache<CacheKey, byte[]> cache;
	private final MapDir[] mapDir;
	private final Adapter adapter;
	private final PackedDataFile[] pdf;

	public MapDescr(String name, Adapter adapter, MapDir[] mapDir)
			throws FileNotFoundException {
		super(name);
		this.pdf = new PackedDataFile[mapDir.length];
		// this.cache = cache;
		this.adapter = adapter;
		this.mapDir = mapDir;

		for (int i = 0; i < mapDir.length; i++) {
			pdf[i] = new PackedDataFile(new File(mapDir[i].dataPath),
					mapDir[i].dataOff);
		}
	}

	@Override
	protected boolean hasTile(int nx, int ny, int zoom) {
		for (MapDir d : mapDir)
			if (d.getOffset(nx, ny, zoom) != -1)
				return true;
		return false;
	}

	@Override
	protected void loadAsync(int nx, int ny, int zoom, int x, int y, int sz,
			Img img) {
		for (int i = 0; i < mapDir.length && img.transparent; i++)
			loadTileAsync(i, nx, ny, zoom, x, y, sz, img);
	}

	private void loadTileAsync(int mapIdx, int nx, int ny, int zoom, int x,
			int y, int sz, Img img) {
		// System.out.println("loading tile " + nx + " " + ny + " " + zoom);

		InputStream is = getInputStreamAsync(mapIdx, nx, ny, zoom);
		if (is == null)
			return;

		DataInputStream dis = new DataInputStream(is);

		try {
			dis.readInt(); // flags
		} catch (IOException e) {
			return;
		}

		// boolean transparent = (flags & 1) != 0;
		Object bm = null;
		try {
			bm = adapter.decodeBitmap(dis);
			if (bm == null)
				return;
			adapter.drawUnder(img.img, bm, x, y, sz);
		} finally {
			if (bm != null)
				adapter.disposeBitmap(bm);
		}

		img.transparent = adapter.isTransparent(img.img);
		// img.transparent &= transparent;
	}

	@SuppressWarnings("unused")
	private synchronized InputStream getInputStreamAsync(int mapIdx, int nx,
			int ny, int z) {
		int off = mapDir[mapIdx].getOffset(nx, ny, z);
		if (off == -1)
			return null;

		if (false) {
			try {
				return new ByteArrayInputStream(pdf[mapIdx].getBytes(off));
			} catch (IOException e) {
				return null;
			}
		} else {
			CacheKey key = new CacheKey(getName(), off);
			synchronized (cache) {
				try {
					byte[] buf = cache.get(key);
					if (buf == null) {
						try {
							buf = pdf[mapIdx].getBytes(off);
							cache.put(key, buf);
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}
					return new ByteArrayInputStream(buf);
				} catch (Throwable e) {
					e.printStackTrace();
					return null;
				}
			}
		}
	}

}