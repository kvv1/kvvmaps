package kvv.ozf;

import java.io.FileNotFoundException;

import kvv.utils.Cache;


public class OzfFile {
	private static native int open(String path);

	private static native void close(int handle);

	private static native int[] getTile(int handle, int x, int y, int z);

	private static native int getNumTilesX(int handle, int z);

	private static native int getNumTilesY(int handle, int z);

	private int handle;
	private int z;
	private int w;
	private int h;

	// private LinkedHashMap<Integer, int[]> cache = new LinkedHashMap<Integer,
	// int[]>() {
	// private static final long serialVersionUID = 1L;
	//
	// protected boolean removeEldestEntry(
	// java.util.Map.Entry<Integer, int[]> eldest) {
	// return size() > 400;
	// };
	// };

	Cache<int[]> cache = new Cache<int[]>(100);

	public OzfFile(String path, int z) throws FileNotFoundException {
		handle = open(path);
		this.z = z;
		if (handle == 0) {
			throw new FileNotFoundException(path);
		}
		w = getNumTilesX(handle, z) * 64;
		h = getNumTilesY(handle, z) * 64;
	}

	@Override
	protected void finalize() throws Throwable {
		if (handle != 0)
			close(handle);
		handle = 0;
		super.finalize();
	}

	public final int getWidth() {
		return w;
	}

	public final int getHeight() {
		return h;
	}

	public int getPixel(int x, int y) {
		if (x < 0 || x >= w || y < 0 || y >= h)
			return 0;
		int[] pixels = getTile(x >> 6, y >> 6);
		int pixel = pixels[(y & 63) * 64 + (x & 63)];
		int r = pixel & 0xFF;
		int g = (pixel >> 8) & 0xFF;
		int b = (pixel >> 16) & 0xFF;
		return  0xFF000000 | (r << 16) | (g << 8) | b;
	}

	private int[] getTile(int tx, int ty) {
		int id = (ty << 16) + tx;

		int[] pixels = cache.get(id);
		if (pixels == null) {
			pixels = getTile(handle, tx, ty, z);
			cache.put(id, pixels);
		}

		return pixels;
	}

	static {
		System.loadLibrary("ozflib");
	}
}
