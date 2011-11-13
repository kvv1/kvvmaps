package kvv.convert;

import java.awt.Image;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import kvv.utils.Cache;
import kvv.utils.IntHashMap;

public class YandexMap implements MapDescr {

	private int minY;
	private int minX;
	private int h;
	private int w;

	public YandexMap(File dir, int zoom) {
		File zoomDir = new File(dir, "z" + zoom);
		String[] yys = zoomDir.list();

		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;

		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;

		for (String ys : yys) {
			int y = Integer.parseInt(ys);
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
			String[] xxs = new File(zoomDir, "" + y).list();
			for (String xs : xxs) {
				int x = Integer.parseInt(xs);
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
			}
		}

		this.minY = minY;
		this.minX = minX;
		this.h = (maxY - minY + 1) * 256;
		this.w = (maxX - minX + 1) * 256;
	}

	private Cache<Image> cache = new Cache<Image>(400);
	private IntHashMap<Integer> empty = new IntHashMap<Integer>();

	@Override
	public int getRGB(int x, int y) {
		int tx = x >>> 8;
		int ty = y >>> 8;
		int key = x + (y << 16);

		if(empty.containsKey(key))
			return 0;
		
		Image img = cache.get(key);
		if(img == null) {
			
			cache.put(key, img);
		}

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getHeight() {
		return h;
	}

}
