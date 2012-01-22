package kvv.kvvmap.adapter;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import kvv.kvvmap.common.tiles.Tiles;

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
	public static boolean debugDraw;

	private final Thread uiThread;
	
	private final ExecutorService executor;

	public Adapter() {
		uiThread = Thread.currentThread();
//		executor = Executors.newSingleThreadExecutor();
		executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(Thread.MIN_PRIORITY);
				return t;
			}
		});
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

	public Object allocBitmap(int w, int h) {
		return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
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

	public void execUI(Runnable runnable) {
		SwingUtilities.invokeLater(runnable);
	}
	
	public void execBG(Runnable runnable) {
		executor.execute(runnable);
	}

	public static void log(String string) {
		System.out.println(string);
	}

	public static void logMem() {
		// TODO Auto-generated method stub
	}

	public void addRecycleable(Tiles tiles) {
		// TODO Auto-generated method stub
	}

	public void drawUnder(Object imgDst, Object imgSrc, int x, int y, int sz) {
		BufferedImage dst = (BufferedImage) imgDst;
		BufferedImage src = (BufferedImage) imgSrc;
		Graphics2D g = (Graphics2D) dst.getGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
		g.drawImage(src, 0, 0, TILE_SIZE, TILE_SIZE, x, y, x + sz, y + sz, null);
		g.dispose();
	}

}
