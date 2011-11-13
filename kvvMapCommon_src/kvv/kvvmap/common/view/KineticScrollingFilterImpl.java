package kvv.kvvmap.common.view;

import java.util.Timer;
import java.util.TimerTask;

public abstract class KineticScrollingFilterImpl implements KineticScrollingFilter {

	@Override
	public abstract void onMousePressed(int x, int y);

	@Override
	public abstract void onMouseReleased(int x, int y);

	@Override
	public abstract void onMouseDragged(int x, int y);

	@Override
	public abstract void exec(Runnable r);

	private static final int FPS = 10;
	private float tx, ty;
	private float cx, cy;
	private float vx, vy;
	private boolean pressed;

	private Timer timer = new Timer();

	public void mousePressed(int x, int y) {
		if (timer != null) {
			timer.cancel();
			timer = null;
			onMouseReleased(x, y);
		}
		onMousePressed(x, y);
		tx = cx = x;
		ty = cy = y;
		vx = vy = 0;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				exec(r);
			}
		}, 0, FPS);
		pressed = true;
	}

	public void mouseReleased(int x, int y) {
		pressed = false;
	}

	public void mouseDragged(int x, int y) {
		if (pressed) {
			cx = x;
			cy = y;
			onMouseDragged(x, y);
		}
	}

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			if (timer != null) {
				if (pressed) {
					float dx = cx - tx;
					float dy = cy - ty;

					tx = cx;
					ty = cy;

					vx = vx * 0.4f + dx * 0.6f;
					vy = vy * 0.4f + dy * 0.6f;
				} else {
					onMouseDragged((int) cx, (int) cy);
					cx += vx;
					cy += vy;
					vx *= 0.95f;
					vy *= 0.95f;

					if (Math.abs(vx) < 0.5 && Math.abs(vy) < 0.5) {
						timer.cancel();
						timer = null;
						onMouseReleased((int) cx, (int) cy);
					}
				}
			}
		}
	};
}
