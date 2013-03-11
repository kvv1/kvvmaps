package kvv.kvvmap.common.maps;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import kvv.kvvmap.common.IntToIntMap;
import kvv.kvvmap.common.Utils;

public class MapDir {
	private final IntToIntMap mapDir = new IntToIntMap();

	public volatile String dataPath;
	public volatile int dataOff;

	private static final int TAG_DIR = 0;
	private static final int TAG_PACK = 1;

	static class Bounds {
		int minx = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;;
		int miny = Integer.MAX_VALUE;;
		int maxy = Integer.MIN_VALUE;;

		void add(int x, int y) {
			minx = Math.min(minx, x);
			maxx = Math.max(maxx, x);
			miny = Math.min(miny, y);
			maxy = Math.max(maxy, y);
		}
	}

	private final Bounds[] bounds = new Bounds[Utils.MAX_ZOOM + 1];

	private void add(int x, int y, int z) {
		if (bounds[z] == null)
			bounds[z] = new Bounds();
		bounds[z].add(x, y);
	}

	private void add(long n) {
		int x = (int) (n >>> 48);
		int y = (int) (n >>> 32) & 0xFFFF;
		int z = (int) (n >>> 28) & 0x000F;
		add(x, y, z);
	}

	// private static int cnt;

	public MapDir(File file, boolean newFormat) throws IOException {

		String path = file.getAbsolutePath();

		if (newFormat)
			this.dataPath = path;
		else
			this.dataPath = path.substring(0, path.lastIndexOf(".dir"))
					+ ".pac";

		// Adapter.log("MapDir " + ++cnt);
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		try {
			if (newFormat) {
				int off = 0;
				for (;;) {
					int len = raf.readInt();
					int tag = raf.readInt();
					if (tag == TAG_DIR) {
						int cnt = (len - 8) / 8;
						while (cnt-- > 0) {
							long n = raf.readLong();
							mapDir.add(n);
							add(n);
						}
					} else if (tag == TAG_PACK) {
						dataOff = off + 8;
					}

					off += len;
					raf.seek(off);
				}
			} else {
				while (true) {
					long n = raf.readLong();
					mapDir.add(n);
					add(n);
				}
			}

		} catch (EOFException e) {
		} finally {
			raf.close();
		}
		mapDir.sort();
	}

	public static void skip(RandomAccessFile dis, int toSkip)
			throws IOException {
		while (toSkip > 0)
			toSkip -= dis.skipBytes(toSkip);
	}

	public int getOffset(int nx, int ny, int z) {
		Bounds b = bounds[z];
		if (b == null)
			return -1;

		if (nx > b.maxx || nx < b.minx || ny > b.maxy || ny < b.miny)
			return -1;

		long n = 0;
		n += nx;
		n <<= 16;
		n += ny;
		n <<= 4;
		n += z;
		n <<= 28;

		int idx1 = mapDir.getIdx(n, 0xFFFFFFFFF0000000L);
		int idx = idx1;
		if (idx == -1)
			return -1;
		return (int) ((mapDir.getAt(idx) & ~0xFFFFFFFFF0000000L) << 4);
	}

	@Override
	protected void finalize() throws Throwable {
		// Adapter.log("~MapDir " + --cnt);
		super.finalize();
	}
}
