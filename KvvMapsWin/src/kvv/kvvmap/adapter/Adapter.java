package kvv.kvvmap.adapter;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class Adapter {
	public final static String ROOT = "c:/kvvMaps";
	public final static String MAPS_ROOT = ROOT + "/maps";
	public final static String PATH_ROOT = ROOT + "/paths";
	public static final String PLACEMARKS = ROOT + "/placemarks.pms";
	public static final int MAP_TILES_CACHE_SIZE = 20;
	public static final int PATH_TILES_CACHE_SIZE = 20;
	public static final int RAF_CACHE_SIZE = 50;

	public static final int TILE_SIZE = 256;
	public static final int TILE_SIZE_G = 256;

	private final Thread uiThread;

	public Adapter() {
		uiThread = Thread.currentThread();
	}

	public synchronized void disposeBitmap(Object img) {
	}

	public synchronized Object allocBitmap() {
		return new BufferedImage(TILE_SIZE, TILE_SIZE,
				BufferedImage.TYPE_INT_ARGB);
	}

	public void drawOver(Object imgDst, Object imgSrc) {
		BufferedImage dst = (BufferedImage) imgDst;
		BufferedImage src = (BufferedImage) imgSrc;
		Graphics g = dst.getGraphics();
		g.drawImage(src, 0, 0, null);
	}

	public void drawOver(Object imgDst, Object imgSrc, int x, int y, int sz) {
		BufferedImage dst = (BufferedImage) imgDst;
		BufferedImage src = (BufferedImage) imgSrc;
		Graphics g = dst.getGraphics();
		g.drawImage(src, 0, 0, TILE_SIZE, TILE_SIZE, x, y, x + sz, y + sz, null);
		g.dispose();
	}

	public Object decodeBitmap(InputStream is) {
		try {
			BufferedImage img = ImageIO.read(is);
			return img;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public GC getGC(Object bm) {
		BufferedImage img = (BufferedImage) bm;
		return new GC((Graphics2D) img.getGraphics(), img.getWidth(),
				img.getHeight());
	}

	public void assertUIThread() {
		if (Thread.currentThread() != uiThread)
			throw new IllegalThreadException();
	}

	static class IllegalThreadException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	public void exec(Runnable runnable) {
		SwingUtilities.invokeLater(runnable);
	}

	public static void log(String string) {
		System.out.println(string);
	}
}
