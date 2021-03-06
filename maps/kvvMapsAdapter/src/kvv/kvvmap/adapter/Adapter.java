package kvv.kvvmap.adapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import kvv.kvvmap.util.Recycleable;
import kvv.kvvmap.util.Utils;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;

import com.smartbean.androidutils.util.StorageUtils;
import com.smartbean.androidutils.util.StorageUtils.StorageInfo;

public class Adapter {
	public static final String ROOT_INT = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/kvvMaps";
	public static final String PATH_ROOT = ROOT_INT + "/paths";
	public static final String PLACEMARKS = ROOT_INT + "/placemarks.pms";

	public final static String MAPS_ROOT;

	static {
		String extRootPath;
		if (Build.VERSION.SDK_INT < 14) {
			List<StorageInfo> sl = StorageUtils.getStorageList();
			extRootPath = sl.get(sl.size() - 1).path + "/kvvMaps";
		} else {
			String[] roots = StorageUtils.getStorageDirectories();
			extRootPath = roots[roots.length-1] + "/kvvMaps";
		}

		if (new File(extRootPath).exists())
			MAPS_ROOT = extRootPath + "/maps";
		else
			MAPS_ROOT = ROOT_INT + "/maps";
	}

	public static int TILE_SIZE_0;
	public static int TILE_SIZE;

	public static int MAP_TILES_CACHE_SIZE;
	// public static int PATH_TILES_CACHE_SIZE;

	public static int RAF_CACHE_SIZE;

	private List<Bitmap> freeBitmaps = new ArrayList<Bitmap>();

	private final Thread uiThread;

	private final Handler handler;

	private final ExecutorService executor;

	public Adapter(Activity context) {

		handler = new Handler();
		uiThread = Thread.currentThread();
		freeBitmaps = new ArrayList<Bitmap>();
		// for (int i = 0; i < MAP_TILES_CACHE_SIZE; i++) {
		// Bitmap bm = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE,
		// Bitmap.Config.ARGB_4444);
		// cnt++;
		// // bm.setDensity(Bitmap.DENSITY_NONE);
		// freeBitmaps.add(bm);
		// }

		// executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		executor = Executors.newFixedThreadPool(4, new ThreadFactory() {
			// executor = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				t.setPriority((Thread.MIN_PRIORITY + Thread.MAX_PRIORITY) / 2);
				return t;
			}
		});
	}

	private float scaleFactor;

	public void setScaleFactor(float f) {
		this.scaleFactor = f;
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	public synchronized void setTileSize(int sz, int widthPixels,
			int heightPixels) {
		freeBitmaps.clear();

		TILE_SIZE = sz;

		int cachesz = (widthPixels / Adapter.TILE_SIZE + 2)
				* (heightPixels / Adapter.TILE_SIZE + 2);
		cachesz = cachesz * 2;

		MAP_TILES_CACHE_SIZE = cachesz;
		RAF_CACHE_SIZE = cachesz * 2;
	}

	public synchronized void recycleBitmap(Object img) {
		if (img == null)
			return;
		Bitmap bm = (Bitmap) img;
		freeBitmaps.add(bm);
	}

	public synchronized void disposeBitmap(Object img) {
		if (img == null)
			return;
		Bitmap bm = (Bitmap) img;
		recycle(bm);
	}

	public Object allocBitmap(int w, int h) {
		try {
			Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
			return bm;
		} catch (OutOfMemoryError e) {
			int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			Log.e("Adapter", "Native mem usage: " + usedMegs);
			e.printStackTrace();
			return null;
		}
	}

	public synchronized Object allocBitmap() {
		if (freeBitmaps.isEmpty()) {
			Object bm = allocBitmap(TILE_SIZE, TILE_SIZE);
			return bm;
		}

		Bitmap bm = freeBitmaps.remove(0);
		bm.eraseColor(0);
		return bm;
	}

	public Object decodeBitmap(InputStream is) {
		try {
			Bitmap bm = BitmapFactory.decodeStream(is);
			// if(bm1 == null)
			// bm1 = BitmapFactory.decodeStream(is);
			// return bm1;
			return bm;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

	public GC getGC(Object bm) {
		Bitmap bm1 = (Bitmap) bm;
		Canvas c1 = new Canvas(bm1);
		Paint paint = new Paint();
		return new GC(c1, paint, bm1.getWidth(), bm1.getHeight(), scaleFactor);
	}

	private Set<Recycleable> recycleables = new HashSet<Recycleable>();
	public static boolean debugDraw = false;

	public synchronized void addRecycleable(Recycleable recycleable) {
		recycleables.add(recycleable);
	}

	public synchronized void recycle() {
		Adapter.log("--dispose free bitmaps");
		for (Bitmap bm : freeBitmaps) {
			recycle(bm);
		}
		// Adapter.log("--");
		freeBitmaps.clear();
		for (Recycleable r : recycleables)
			r.recycle();
		recycleables = new HashSet<Recycleable>();
		log("****recycled");

		executor.shutdown();
	}

	public void assertUIThread() {
		if (Thread.currentThread() != uiThread) {
			final Throwable t = new IllegalThreadException();
			t.printStackTrace();
			execUI(new Runnable() {
				@Override
				public void run() {
					// Toast.makeText(context, "IllegalThreadException",
					// Toast.LENGTH_LONG);
					try {
						OutputStream errOutputStream = new FileOutputStream(
								Adapter.ROOT_INT + "/" + System.currentTimeMillis()
										+ ".log");
						PrintStream errPrintStream = new PrintStream(
								errOutputStream, true);
						t.printStackTrace(errPrintStream);
						errOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			// throw new IllegalThreadException();
		}
	}

	static class IllegalThreadException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	public void execUI(Runnable runnable) {
		handler.post(runnable);
	}

	public void execBG(final Runnable runnable) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					log(sw.toString());
				}
			}
		});
	}

	public static void log(String string) {
		Log.w("KVVMAPS", string);
		appendLog(string);
	}

	private static void recycle(Bitmap bm) {
		bm.recycle();
	}

	@Override
	protected void finalize() throws Throwable {
		log("~Adapter");
		super.finalize();
	}

	public boolean isTransparent(Object img) {
		Bitmap bm = (Bitmap) img;

		final int n = 8;

		int w = bm.getWidth();
		int h = bm.getHeight();

		int dw = w / n / 2;
		int dh = h / n / 2;

		for (int i = 0; i < n; i++) {
			int y = h * i / n + dh;
			for (int j = 0; j < n; j++) {
				int x = w * j / n + dw;
				if ((bm.getPixel(x, y) & 0xFF000000) == 0)
					return true;
			}
		}

		return false;
	}

	public void drawUnder(Object imgDst, Object imgSrc, int x, int y, int sz) {
		Bitmap dst = (Bitmap) imgDst;
		Bitmap src = (Bitmap) imgSrc;

		int srcW = src.getWidth();
		int srcH = src.getHeight();

		Canvas c = new Canvas(dst);
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
		c.drawBitmap(src, new Rect(x * srcW / Utils.TILE_SIZE_G, y * srcH
				/ Utils.TILE_SIZE_G, (x + sz) * srcW / Utils.TILE_SIZE_G,
				(y + sz) * srcH / Utils.TILE_SIZE_G), new Rect(0, 0, TILE_SIZE,
				TILE_SIZE), paint);
	}

	public static long getNativeHeapAllocatedSize() {
		return Debug.getNativeHeapAllocatedSize();
	}

	public static long getNativeHeapFreeSize() {
		return Debug.getNativeHeapFreeSize();
	}

	public static long getNativeHeapSize() {
		return Debug.getNativeHeapSize();
	}

	public int getBitmapWidth(Object img) {
		return ((Bitmap) img).getWidth();
	}

	public static void appendLog(String text) {
		text = DateFormat.format("MM/dd/yy hh:mm:ss", new Date()) + " " + text;

		File logFile = new File(ROOT_INT + "/log.txt");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
