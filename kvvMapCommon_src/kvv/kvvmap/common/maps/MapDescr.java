package kvv.kvvmap.common.maps;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.common.Cache;
import kvv.kvvmap.common.Img;
import kvv.kvvmap.common.PackedDataFile;
import kvv.kvvmap.common.maps.Maps.CacheKey;

public class MapDescr extends MapDescrBase {
	private final Cache<CacheKey, byte[]> cache;
	private final MapDir[] mapDir;
	private final Adapter adapter;
	private final PackedDataFile[] pdf;

	public MapDescr(String name, Cache<CacheKey, byte[]> cache, 
			Adapter adapter, MapDir[] mapDir) throws FileNotFoundException {
		super(name);
		this.pdf = new PackedDataFile[mapDir.length];
		this.cache = cache;
		this.adapter = adapter;
		this.mapDir = mapDir;

		for (int i = 0; i < mapDir.length; i++) {
			pdf[i] = new PackedDataFile(new File(mapDir[i].filePath.substring(
					0, mapDir[i].filePath.lastIndexOf(".dir")) + ".pac"));
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
	protected Img load(int nx, int ny, int zoom, int x, int y, int sz, Img img) {
		for (int i = 0; i < mapDir.length; i++) {
			if (img != null && !img.transparent)
				return img;

			img = loadTile(i, nx, ny, zoom, x, y, sz, img);
		}

		return img;
	}

	private Img loadTile(int idx, int nx, int ny, int zoom, int x, int y,
			int sz, Img imgBase) {
		// System.out.println("loading tile " + nx + " " + ny + " " + zoom);

		InputStream is = getInputStream(idx, nx, ny, zoom);
		if (is == null)
			return imgBase;

		DataInputStream dis = new DataInputStream(is);

		int flags;
		try {
			flags = dis.readInt();
		} catch (IOException e) {
			return imgBase;
		}

		boolean transparent = (flags & 1) != 0;

		Object bm = adapter.decodeBitmap(dis);
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

		return new Img(img1, transparent);
	}

	private synchronized InputStream getInputStream(int idx, int nx, int ny,
			int z) {
		int off = mapDir[idx].getOffset(nx, ny, z);
		if (off == -1)
			return null;

		Maps.CacheKey key = new Maps.CacheKey(this, off);

		byte[] buf = cache.get(key);
		if (buf == null) {
			try {
				buf = pdf[idx].getBytes(off);
				cache.put(key, buf);
			} catch (IOException e) {
				return null;
			}
		}
		return new ByteArrayInputStream(buf);
	}

}