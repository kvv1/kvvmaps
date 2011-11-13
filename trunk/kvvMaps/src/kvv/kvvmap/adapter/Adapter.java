package kvv.kvvmap.adapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class Adapter {
	public final static String ROOT = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/kvvMaps";
	public final static String MAPS_ROOT = ROOT + "/maps";
	public static final String PATH_ROOT = ROOT + "/paths";
	public static final String PLACEMARKS = ROOT + "/placemarks.pms";

	public static int TILE_SIZE = 256;

	public static int MAP_TILES_CACHE_SIZE;
	public static int PATH_TILES_CACHE_SIZE;

	public static int RAF_CACHE_SIZE;

	private List<Bitmap> freeBitmaps;

	private final Thread uiThread;

	private final Handler handler;

	private static volatile int cnt;

	public Adapter(Context context) {
		handler = new Handler();
		uiThread = Thread.currentThread();
	}

	public synchronized void disposeBitmap(Object img) {
		if (img == null)
			return;
		Bitmap bm = (Bitmap) img;
		if (bm.isMutable()) {
			// Adapter.log("add to free bitmaps");
			freeBitmaps.add(bm);
			// Log.i("Adapter", "freeBitmaps " + freeBitmaps.size());
		} else {
			// Adapter.log("recycle");
			recycle(bm);
		}
	}

	public synchronized Object allocBitmap() {
		if (freeBitmaps == null) {
			freeBitmaps = new ArrayList<Bitmap>();
			for (int i = 0; i < MAP_TILES_CACHE_SIZE + PATH_TILES_CACHE_SIZE; i++) {
				Bitmap bm = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE,
						Bitmap.Config.ARGB_4444);
				cnt++;
				// bm.setDensity(Bitmap.DENSITY_NONE);
				freeBitmaps.add(bm);
			}
		}

		if (freeBitmaps.isEmpty()) {
			try {
				Bitmap bm = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE,
						Bitmap.Config.ARGB_4444);
				cnt++;
				// bm.setDensity(Bitmap.DENSITY_NONE);
				return bm;
			} catch (OutOfMemoryError e) {
				int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				Log.e("Adapter", "Native mem usage: " + usedMegs);
				e.printStackTrace();
				return null;
			}
		}

		Bitmap bm = freeBitmaps.remove(0);
		bm.eraseColor(0);
		return bm;
	}

	public void drawOver(Object imgDst, Object imgSrc) {
		Canvas c = new Canvas((Bitmap) imgDst);
		c.drawBitmap((Bitmap) imgSrc, 0, 0, null);
	}

	// private static volatile Bitmap bm1;

	public Object decodeBitmap(InputStream is) {
		try {
			Bitmap bm = BitmapFactory.decodeStream(is);
			cnt++;
			// if(bm1 == null)
			// bm1 = BitmapFactory.decodeStream(is);
			// return bm1;
			return bm;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

	public void drawOver(Object imgDst, Object imgSrc, int x, int y, int sz) {
		Canvas c = new Canvas((Bitmap) imgDst);
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		c.drawBitmap((Bitmap) imgSrc, new Rect(x, y, x + sz, y + sz), new Rect(
				0, 0, TILE_SIZE, TILE_SIZE), paint);
	}

	public GC getGC(Object bm) {
		Bitmap bm1 = (Bitmap) bm;
		Canvas c1 = new Canvas(bm1);
		Paint paint = new Paint();
		return new GC(c1, paint, bm1.getWidth(), bm1.getHeight());
	}

	public synchronized void dispose() {
		// Adapter.log("--dispose free bitmaps");
		for (Bitmap bm : freeBitmaps) {
			recycle(bm);
		}
		// Adapter.log("--");
		freeBitmaps.clear();
	}

	public void assertUIThread() {
		if (Thread.currentThread() != uiThread) {
			final Throwable t = new IllegalThreadException();
			t.printStackTrace();
			exec(new Runnable() {
				@Override
				public void run() {
					// Toast.makeText(context, "IllegalThreadException",
					// Toast.LENGTH_LONG);
					try {
						OutputStream errOutputStream = new FileOutputStream(
								Adapter.ROOT + "/" + System.currentTimeMillis()
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

	public void exec(Runnable runnable) {
		handler.post(runnable);
	}

	public static void log(String string) {
		Log.w("KVVMAPS", string);
	}

	private static void recycle(Bitmap bm) {
		bm.recycle();
		cnt--;
		// Adapter.log("recycle " + cnt);
		// Adapter.log("mem: free=" + Runtime.getRuntime().freeMemory() / 1024 /
		// 1024 + " total="
		// + Runtime.getRuntime().totalMemory() / 1024 / 1024);
	}

	// public static void beep() {
	// try {
	// MyActivity.mediaPlayer.start();
	// } catch (Exception e) {
	// Log.e("beep", "error: " + e.getMessage(), e);
	// }
	// }

}
