package kvv.aplayer.files;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import kvv.aplayer.R;
import kvv.aplayer.files.TextView1.ScrollListener;
import kvv.aplayer.player.Files;
import kvv.aplayer.player.Player.OnChangedHint;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TextSectionFragment extends FilesSectionFragmentBase {

	private static final int STEP = 1000;

	private TextView1 textView;
	private Handler handler = new Handler();

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			if (conn.service != null && conn.service.isPlaying()
					&& textView != null) {
				int scrollY = textView.getScrollY();
				textView.scrollTo(0, scrollY + (int) dScroll(STEP));
			}
			handler.postDelayed(this, STEP);
		}
	};

	public TextSectionFragment() {
		super(APService.class, R.layout.fragment_text);
	}

	@Override
	protected void trackChanged() {
		super.trackChanged();
		textView.setText("");
		Files files = conn.service.getFiles();
		if (files != null && files.curFile >= 0) {
			FileDescriptor fileDescriptor = files.files.get(files.curFile);
			int idx = fileDescriptor.path.indexOf('.');
			String textPath = fileDescriptor.path.substring(0, idx) + ".txt";
			BufferedReader rd = null;
			try {
				rd = new BufferedReader(new InputStreamReader(
						new FileInputStream(textPath), "utf8"));
				String line;
				while ((line = rd.readLine()) != null) {
					textView.append(line + "\n");
				}
			} catch (Exception e) {
			} finally {
				if (rd != null)
					try {
						rd.close();
					} catch (IOException e) {
					}
			}
		}

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				textView.scrollTo(0, calcScrollY());
			}
		}, 200);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void createUI(final IAPService service) {
		super.createUI(service);

		textView = (TextView1) rootView.findViewById(R.id.text);
		textView.setMovementMethod(new ScrollingMovementMethod());

		textView.scrollListener = new ScrollListener() {
			@Override
			public void onScroll(int horiz, int vert, int oldHoriz, int oldVert) {

			}
		};
		onChanged(OnChangedHint.FILE);

		Button sync = (Button) rootView.findViewById(R.id.syncText);
		sync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				textView.scrollTo(0, calcScrollY());
			}
		});
	}

	private float dScroll(int ms) {
		int h = textView.getHeight();
		int lh = textView.getLayout().getHeight();
		int durMS = conn.service.getDuration();

		float d = (float) ms * (lh - h) / durMS;

		if (d < 0)
			d = 0;

		return d;
	}

	private int calcScrollY() {
		int h = textView.getHeight();
		int lh = textView.getLayout().getHeight();

		float pos = conn.service.getCurrentPosition() / 1000f;
		float dur = conn.service.getDuration() / 1000f;

		// return -100;
		return (int) (pos * lh / dur - h / 2);

		// float delta = 5;
		// int y = (int) ((pos - delta) * lh / (dur - 2 * delta) - h / 2);
		// return y;

		//
		// if (y > lh - h)
		// y = lh - h;
		// if (y < 0)
		// y = 0;
		//
		// return y;
	}

	@Override
	public void onPause() {
		handler.removeCallbacksAndMessages(null);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		handler.post(r);
	}

	@Override
	public void onLevelChanged(float level) {
	}

	@Override
	public void onLoaded() {
	}

	@Override
	protected boolean isLevelNeeded() {
		return false;
	}

}
