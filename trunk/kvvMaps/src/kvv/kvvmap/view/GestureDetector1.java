package kvv.kvvmap.view;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public class GestureDetector1 {

	private final OnGestureListener listener;

	public GestureDetector1(OnGestureListener listener) {
		this.listener = listener;
		this.detector = new OldDetector();
	}

	public GestureDetector1(Context context, OnGestureListener listener,
			Handler handler, boolean b) {
		this.listener = listener;
		this.detector = new NewDetector();
	}

	public void setIsLongpressEnabled(boolean b) {
	}

	private final Detector detector;

	private PointF p1;
	private long time;
	private float vx;
	private float vy;

	public void onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
	}

	interface Detector {
		void onTouchEvent(MotionEvent event);
	}

	class OldDetector implements Detector {
		public void onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				p1 = new PointF(x, y);
				vx = vy = 0;
				time = event.getEventTime();
				listener.onDown(event);
				break;
			case MotionEvent.ACTION_UP:
				if (Math.sqrt(vx * vx + vy * vy) > 100)
					listener.onFling(null, null, vx, vy);
				p1 = null;
				break;
			case MotionEvent.ACTION_MOVE:
				if (p1 != null) {
					listener.onScroll(null, null, p1.x - x, p1.y - y);
					long dt = event.getEventTime() - time;
					float vx1 = (x - p1.x) * 1000 / dt;
					float vy1 = (y - p1.y) * 1000 / dt;
					vx = vx * 0.7f + vx1 * 0.3f;
					vy = vy * 0.7f + vy1 * 0.3f;
					p1.set(x, y);
					time = event.getEventTime();
				}
			}
		}
	}

	class NewDetector extends OldDetector {
		@Override
		public void onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_POINTER_DOWN:
				if (event.getPointerCount() > 1) {
					p1 = null;
					vx = vy = 0;
				}
				return;
			}
			super.onTouchEvent(event);
		}
	}
}
