package kvv.kvvmap.view;

import kvv.kvvmap.MyActivity;
import kvv.kvvmap.R;
import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.view.CommonView;
import kvv.kvvmap.common.view.Environment;
import kvv.kvvmap.common.view.IPlatformView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.Scroller;

public class MapView extends View implements IPlatformView {

	private CommonView commonView;

	private MyActivity activity;

	private Compass compass;

	private Thread uiThread;

	private OnTouchListener touchListener;

	public void assertUIThread() {
		if (Thread.currentThread() != uiThread) {
			final Throwable t = new RuntimeException("illegal thread");
			t.printStackTrace();
		}
	}

	public MapView(Context ctxt, AttributeSet attrs) {
		super(ctxt, attrs);
		uiThread = Thread.currentThread();
		setFocusable(true);
		setFocusableInTouchMode(true);

		boolean hasMultiTouch = Integer.parseInt(Build.VERSION.SDK) >= 5;
		if (hasMultiTouch)
			touchListener = new MultiTouchListener();
		else
			touchListener = new TouchListener();
	}

	private static int cnt;

	public void init(MyActivity activity, Environment envir,
			Bundle savedInstanceState) {
		assertUIThread();
		Adapter.log("MapView.init " + ++cnt);

		this.activity = activity;
		commonView = new CommonView(this, envir);

		Adapter.logMem();

		animateTo(new LocationX(30, 60));

		if (savedInstanceState != null) {
			double lon = savedInstanceState.getDouble("lon", 30);
			double lat = savedInstanceState.getDouble("lat", 60);
			int zoom = savedInstanceState.getInt("zoom", 1);
			String topMap = savedInstanceState.getString("map");
			commonView.setZoom(zoom);
			animateTo(new LocationX(lon, lat));
			commonView.setTopMap(topMap);
			commonView.fixMap(activity.getFixedMap());
		}

	}

	private void updateTitle() {
		if (activity == null || commonView == null)
			return;

		String lon = Utils.format(commonView.getLocation().getLongitude());
		String lat = Utils.format(commonView.getLocation().getLatitude());

		String mem = "";
		if (Adapter.debugDraw) {
			int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			int freeMegs = (int) (Debug.getNativeHeapFreeSize() / 1048576L);
			int allMegs = (int) (Debug.getNativeHeapSize() / 1048576L);
			mem = usedMegs + " " + freeMegs + " " + allMegs + " ";
		}

		String title = lon + " " + lat + " z" + commonView.getZoom() + " "
				+ mem + commonView.getTopMap();
		activity.setTitle(title);
	}

	public ISelectable getSel() {
		if (commonView == null)
			return null;
		return commonView.getSel();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (commonView != null)
			commonView.onSizeChanged(w, h);
	}

	@Override
	public void onDraw(Canvas canvas) {

		// Adapter.log("Draw");

		if (activity == null || commonView == null) {
			canvas.drawColor(Color.YELLOW);
			return;
		}

		Paint paint = new Paint();

		GC gc = new GC(canvas, paint, getWidth(), getHeight());

		commonView.draw(gc);

		paint.setAntiAlias(true);

		if (commonView.isMultiple()) {
			Bitmap bm = activity.bmMultimap;
			canvas.drawBitmap(bm, getWidth() - bm.getWidth(), 0, null);
		}

		if (activity.isFollowing()) {
			Bitmap bm = activity.bmFollow;
			canvas.drawBitmap(bm, getWidth() - bm.getWidth() * 2, 0, null);
		}

		if (activity.mapsService.getTracker().isTracking()) {
			Bitmap bm = activity.bmWriting;
			canvas.drawBitmap(bm, getWidth() - bm.getWidth() * 3, 0, null);
		}

		if (activity.getFixedMap() != null) {
			Bitmap bm = activity.bmFixedMap;
			canvas.drawBitmap(bm, getWidth() - bm.getWidth() * 4, 0, null);
		}

		if (compass == null)
			compass = new Compass(getWidth() / 10);

		Float targBearing = null;
		LocationX myLoc = commonView.getMyLocation();
		LocationX targ = commonView.getTarget();
		if (myLoc != null && targ != null && !commonView.isMyLocationDimmed())
			targBearing = myLoc.bearingTo(targ);

		compass.drawCompass(canvas, paint, new Point(getWidth() - getWidth()
				/ 10, getHeight() / 10), targBearing);

		if (activity.mapsService.isLoadingMaps()) {
			paint.setColor(Color.CYAN);
			paint.setTextSize(24);
			canvas.drawText("Загрузка карт...", 10, 100, paint);
		}

	}

	class MultiTouchListener implements OnTouchListener {
		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}

		private PointInt p1;

		float oldDist;

		int DRAG = 2;
		int ZOOM = 1;
		int NONE = 0;
		int mode;

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {

			if (commonView == null)
				return true;

			int x = (int) (event.getX());
			int y = (int) (event.getY());
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				p1 = new PointInt(x, y);
				commonView.startScrolling();
				mode = DRAG;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				p1 = null;
				commonView.endScrolling();
				mode = NONE;
				break;

			case MotionEvent.ACTION_POINTER_DOWN:
				if (event.getPointerCount() == 2) {
					oldDist = spacing(event);
					if (oldDist > 10f)
						mode = ZOOM;
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					if (p1 != null) {
						commonView.scrollBy(p1.x - x, p1.y - y);
						p1 = new PointInt(x, y);
					}
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						float scale = newDist / oldDist;
						int dx = (int) ((event.getX(0) + event.getX(1)) / 2)
								- getWidth() / 2;
						int dy = (int) ((event.getY(0) + event.getY(1)) / 2)
								- getHeight() / 2;

						LocationX loc = commonView.getLocation(dx, dy);

						if (scale > 1.5) {
							zoomIn();
							commonView.animateTo(loc, dx, dy);
							mode = NONE;
						} else if (scale < 0.75) {
							zoomOut();
							commonView.animateTo(loc, dx, dy);
							mode = NONE;
						}
					}
				}
				break;
			}
			return true;
		}
	}

	class TouchListener implements OnTouchListener {

		private PointInt p1;

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			if (commonView == null)
				return true;

			int x = (int) (event.getX());
			int y = (int) (event.getY());
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				p1 = new PointInt(x, y);
				commonView.startScrolling();
				break;
			case MotionEvent.ACTION_UP:
				p1 = null;
				commonView.endScrolling();
				break;
			case MotionEvent.ACTION_MOVE:
				if (p1 != null) {
					commonView.scrollBy(p1.x - x, p1.y - y);
					p1 = new PointInt(x, y);
				}
			}
			return true;
		}

	}

	private Runnable endScrollNotifier = new Runnable() {
		@Override
		public void run() {
			commonView.endScrolling();
		}
	};

	private void move() {
		commonView.startScrolling();
		removeCallbacks(endScrollNotifier);
		postDelayed(endScrollNotifier, 200);
	}

	private final Scroller mScroller = new Scroller(getContext());
	private int oldx;
	private int oldy;

	private Runnable scrollerRunnable = new Runnable() {
		@Override
		public void run() {

			if (!mScroller.computeScrollOffset())
				return;
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();

			move();
			commonView.scrollBy(x - oldx, y - oldy);
			oldx = x;
			oldy = y;

			// System.out.println("scroll " + x + " " + y);

			if (!mScroller.isFinished())
				post(this);

		}
	};

	private class MyOnGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			move();
			commonView.scrollBy(distanceX, distanceY);
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float vX,
				float vY) {

			if (!activity.isKineticScrolling())
				return true;

			int max = 400;

			int v = (int) Math.sqrt(vX * vX + vY * vY);

			if (v > max) {
				vX = vX * max / v;
				vY = vY * max / v;
			}

			// System.out.println("onFling " + vX + " " + vY);
			mScroller.fling(oldx, oldy, -(int) vX, -(int) vY,
					Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
					Integer.MAX_VALUE);

			removeCallbacks(scrollerRunnable);
			post(scrollerRunnable);
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			if (!mScroller.isFinished()) { // is flinging
				mScroller.forceFinished(true); // to stop flinging on
												// touch
			}
			return true; // else won't work
		}
	}

	private class MyGestureDetector extends GestureDetector1 {
		public MyGestureDetector() {
			super(new MyOnGestureListener());
		}

		{
			setIsLongpressEnabled(false);
		}

	}

	private class MyGestureDetector22 extends GestureDetector1 {
		public MyGestureDetector22() {
			super(getContext(), new MyOnGestureListener(), new Handler(), true);
		}

		{
			setIsLongpressEnabled(false);
		}
	}

	private class MyScaleGestureDetector {
		private final ScaleGestureDetector mScaleDetector;

		MyScaleGestureDetector(Context context) {
			mScaleDetector = new ScaleGestureDetector(context,
					new ScaleGestureDetector.SimpleOnScaleGestureListener() {
						int initZoom;
						float mScaleFactor; 

						@Override
						public boolean onScaleBegin(
								ScaleGestureDetector detector) {
							initZoom = commonView.getZoom();
							mScaleFactor = 1f;
							return super.onScaleBegin(detector);
						}

						@Override
						public boolean onScale(ScaleGestureDetector detector) {
							Adapter.log("onScale " + detector.getScaleFactor());
							
							mScaleFactor *= detector.getScaleFactor();

							int newZoom = (int) (initZoom + Math.log(mScaleFactor) + 0.5);
							newZoom = Math.max(Utils.MIN_ZOOM,
									Math.min(Utils.MAX_ZOOM, newZoom));

							if (newZoom != commonView.getZoom())
								commonView.setZoom(newZoom);

							return true;
						}
					});
		}

		public void onTouchEvent(MotionEvent event) {
			mScaleDetector.onTouchEvent(event);
		}

	}

	private MyScaleGestureDetector mScaleDetector;
	private GestureDetector1 mGestureDetector;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector == null) {
			if (Integer.parseInt(Build.VERSION.SDK) >= 8)
				mGestureDetector = new MyGestureDetector22();
			else
				mGestureDetector = new MyGestureDetector();
		}

		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			if (mScaleDetector == null)
				mScaleDetector = new MyScaleGestureDetector(getContext());
			mScaleDetector.onTouchEvent(event);
		}

		mGestureDetector.onTouchEvent(event);
		return true;

	}

	public void animateTo(LocationX loc) {
		if (commonView != null)
			commonView.animateTo(loc);
	}

	public void zoomOut() {
		if (commonView != null)
			commonView.zoomOut();
	}

	public void zoomIn() {
		if (commonView != null)
			commonView.zoomIn();
	}

	public void save(Bundle outState) {
		if (commonView != null) {
			if (commonView != null) {
				outState.putDouble("lon", commonView.getLocation()
						.getLongitude());
				outState.putDouble("lat", commonView.getLocation()
						.getLatitude());
				outState.putInt("zoom", commonView.getZoom());
				outState.putString("map", commonView.getTopMap());
			}
		}
	}

	public void setCompass(float[] values) {
		if (compass != null) {
			compass.setValues(values);
			invalidate();
		}
		
		if(commonView != null && values != null && values.length >= 1) {
			//commonView.setAngle(-values[0] * Math.PI / 180);
		}
		
	}

	public LocationX createPlacemark() {
		if (commonView == null)
			return null;

		LocationX pm = commonView.getLocation();
		return pm;
	}

	public void reorderMaps() {
		if (commonView != null)
			commonView.reorderMaps();
	}

	public LocationX getCenter() {
		if (commonView == null)
			return null;
		return commonView.getLocation();
	}

	private final Runnable titleUpdater = new Runnable() {
		@Override
		public void run() {
			updateTitle();
			updateButtons();
		}
	};

	@Override
	public void repaint() {
		postInvalidate();
		removeCallbacks(titleUpdater);
		postDelayed(titleUpdater, 200);
	}

	public void setMyLocation(LocationX locationX, boolean forceScroll) {
		if (commonView == null)
			return;
		commonView.setMyLocation(locationX, forceScroll);
		updateButtons();
	}

	public void dimmMyLocation() {
		if (commonView == null)
			return;
		commonView.dimmMyLocation();
		updateButtons();
	}

	private void updateButtons() {
		if (activity == null || commonView == null)
			return;

		View button = activity.findViewById(R.id.here);
		if (commonView.getMyLocation() != null && !commonView.isOnMyLocation())
			button.setVisibility(View.VISIBLE);
		else
			button.setVisibility(View.GONE);

		Button toTarget = (Button) activity.findViewById(R.id.toTarget);
		LocationX target = commonView.getTarget();
		if (target != null) {
			toTarget.setVisibility(View.VISIBLE);
			int dist = (int) commonView.getLocation().distanceTo(target);
			toTarget.setText(Utils.formatDistance(dist));
		} else {
			toTarget.setVisibility(View.GONE);
		}

		OnScreenButton zoomIn = (OnScreenButton) activity
				.findViewById(R.id.zoomin);
		zoomIn.setEnabled(commonView.getZoom() < Utils.MAX_ZOOM);

		OnScreenButton zoomOut = (OnScreenButton) activity
				.findViewById(R.id.zoomout);
		zoomOut.setEnabled(commonView.getZoom() > Utils.MIN_ZOOM);

		OnScreenButton edit = (OnScreenButton) activity.findViewById(R.id.edit);
		edit.setEnabled(commonView.getSel() != null);

		OnScreenButton iplus = (OnScreenButton) activity
				.findViewById(R.id.infoplus);
		iplus.setEnabled(commonView.getInfoLevel() != InfoLevel.HIGH);

		OnScreenButton iminus = (OnScreenButton) activity
				.findViewById(R.id.infominus);
		iminus.setEnabled(commonView.getInfoLevel() != InfoLevel.LOW);

		OnScreenButton rotate = (OnScreenButton) activity
				.findViewById(R.id.rotate);
		rotate.setEnabled(commonView.isMultiple());
	}

	public void dispose() {
		assertUIThread();
		if (commonView != null) {
			Adapter.log("MapView.dispose " + --cnt);
			commonView.dispose();
		}

		commonView = null;
		compass = null;
		activity = null;
	}

	public void incInfoLevel() {
		if (commonView != null)
			commonView.incInfoLevel();
	}

	public void decInfoLevel() {
		if (commonView != null)
			commonView.decInfoLevel();
	}

	public void invalidatePathTiles() {
		if (commonView != null)
			commonView.invalidatePathTiles();
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~MapView");
		super.finalize();
	}

	public void animateToMyLocation() {
		if (commonView != null)
			commonView.animateToMyLocation();
	}

	public void animateToTarget() {
		if (commonView != null)
			commonView.animateToTarget();
	}

	public String fixMap(boolean fix) {
		if (commonView != null)
			return commonView.fixMap(fix);
		return null;
	}

	@Override
	public boolean loadDuringScrolling() {
		return activity == null || activity.loadDuringScrolling();
	}

}
