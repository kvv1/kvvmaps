package kvv.ecw;

import java.io.FileNotFoundException;

import kvv.utils.Cache;

public class EcwFile {
	private static native int open(String path);

	private static native void close(int handle);

	private static native int[] getTile(int handle, int x, int y, int z);

	private static native int getNumTilesX(int handle, int z);

	private static native int getNumTilesY(int handle, int z);

	private final static int TILE_SZ = 64;

	private String path;

	private int handle;
	private int z;
	private int w;
	private int h;

	Cache<int[]> cache = new Cache<int[]>(100);

	public EcwFile(String path, int z) throws FileNotFoundException {
		this.path = path;
		handle = open(path);
		this.z = z;
		if (handle == 0) {
			throw new FileNotFoundException(path);
		}
		w = getNumTilesX(handle, z) * TILE_SZ;
		h = getNumTilesY(handle, z) * TILE_SZ;
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
		int[] pixels = getTile(x / TILE_SZ, y / TILE_SZ);
		int pixel = pixels[(y % TILE_SZ) * TILE_SZ + (x % TILE_SZ)] | 0xFF000000;
		return pixel;
	}

	int ntile;

	private int[] getTile(int tx, int ty) {

		int id = (ty << 16) + tx;

		int[] pixels = cache.get(id);
		if (pixels == null) {
			// int xx = (int) (Math.random() * 500);
			// int yy = (int) (Math.random() * 500);
			// System.out.println("" + xx + "-" + yy);
			// getTile(handle, xx, yy, z);

			//System.out.println("" + tx + " " + ty);
			pixels = getTile(handle, tx, ty, z);
			cache.put(id, pixels);

			if (ntile++ == 100) {
				ntile = 0;
				close(handle);
				handle = open(path);
			}
		}

		return pixels;
	}

	static {
		System.loadLibrary("ozflib");
	}
}
