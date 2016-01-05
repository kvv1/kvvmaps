package kvv.aplayer.files;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

public class TouchListener {
	
	private float touchX;
	private float touchY;
	private long lastKeyUp;
	private boolean longClick;
	protected Handler handler = new Handler();

	private Runnable seekRunnable = new Runnable() {
		@Override
		public void run() {
			onHold(touchX, touchY);
			handler.postDelayed(this, 200);
		}
	};


	public TouchListener(View view) {
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (System.currentTimeMillis() - lastKeyUp > 500)
					TouchListener.this.onClick(touchX, touchY);
			}
		});

		view.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handler.postDelayed(seekRunnable, 200);
				longClick = true;
				TouchListener.this.onLongClick(touchX, touchY);
				return true;
			}
		});

		view.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchX = event.getX();
				touchY = event.getY();

				if (event.getAction() == MotionEvent.ACTION_UP) {
					onReleased(touchX, touchY);
					handler.removeCallbacks(seekRunnable);
					if (longClick)
						lastKeyUp = System.currentTimeMillis();
					longClick = false;
				}
				return false;
			}
		});
	}
	

	protected void onClick(float touchX, float touchY) {}
	protected void onLongClick(float touchX, float touchY) {}
	protected void onHold(float touchX, float touchY) {}
	protected void onReleased(float touchX, float touchY) {}
}
