package kvv.kvvmap.view;

import kvv.kvvmap.MyActivity;
import kvv.kvvmap.R;
import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
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
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MapView extends View implements IPlatformView {

	private CommonView commonView;

	private MyActivity activity;

	private Compass compass;

	private Thread uiThread;

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
			setOnTouchListener(new MultiTouchListener());
		else
			setOnTouchListener(new TouchListener());
	}

	private static int cnt;

	public void init(MyActivity activity, Environment envir, Bundle savedInstanceState) {
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

		// Adapter.log("Draw " + this);

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
			canvas.drawBitmap(bm, getWidth() - 40, 0, null);
		}

		if (activity.mapsService.getTracker().isTracking()) {
			Bitmap bm = activity.bmWriting;
			canvas.drawBitmap(bm, getWidth() - 60, 0, null);
		}

		if (activity.getFixedMap() != null) {
			Bitmap bm = activity.bmFixedMap;
			canvas.drawBitmap(bm, getWidth() - 80, 0, null);
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

	}

	class MultiTouchListener implements OnTouchListener {
		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}

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
				commonView.onDown(x, y);
				mode = DRAG;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				commonView.onUp(x, y);
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
					commonView.onMove(x, y);
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

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			if (commonView == null)
				return true;

			int x = (int) (event.getX());
			int y = (int) (event.getY());
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				commonView.onDown(x, y);
				break;
			case MotionEvent.ACTION_UP:
				commonView.onUp(x, y);
				break;
			case MotionEvent.ACTION_MOVE:
				commonView.onMove(x, y);
			}
			return true;
		}

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

}
