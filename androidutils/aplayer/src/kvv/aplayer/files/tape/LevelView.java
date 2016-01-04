package kvv.aplayer.files.tape;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.smartbean.androidutils.util.LPF;

public class LevelView extends View {

	private final static int STEP_MS = 30;

	private Handler handler = new Handler();

	public LevelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LevelView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public LevelView(Context context) {
		super(context);
	}

	private float level;

	protected LPF lpf = new LPF(1000 / STEP_MS, 0.1, 0.2);
	protected LPF levelLpf = new LPF(1000 / STEP_MS, 1, 3);
	{
		levelLpf.set(0.1);
	}

	private Runnable r;
	private int cnt;

	public void setLevel(float level) {

		if (getVisibility() != View.VISIBLE)
			return;

		// System.out.println("setLevel " + level);

		if (level < 0)
			level = 0;
		if (level > 1.3)
			level = 1.3f;

		this.level = level;

		cnt = 3000 / STEP_MS; // 3 sec

		if (r == null) {
			r = new Runnable() {
				@Override
				public void run() {
					if (cnt >= 0) {
						lpf.add(LevelView.this.level);
						
						if (cnt > 200 / STEP_MS)
							levelLpf.add(LevelView.this.level * 2);

						if(lpf.get() / levelLpf.get() > 1.3)
							levelLpf.set(lpf.get() / 1.3);
						
						invalidate();

						handler.postDelayed(r, STEP_MS);
						cnt--;
					} else {
						r = null;
					}
				}
			};
			handler.post(r);
		}

	}

	public void setScale(int[] scale) {
	}
}
