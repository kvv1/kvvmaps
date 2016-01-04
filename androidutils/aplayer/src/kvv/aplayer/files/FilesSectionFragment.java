package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.IAPService;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;

import com.smartbean.androidutils.fragment.FragmentX;

public class FilesSectionFragment extends FragmentX<APActivity, IAPService> {

	private SharedPreferences settings;

	protected Handler handler = new Handler();

	protected void restartButtonsTimer() {
	}

	private int seekStep = 0;

	private Runnable seekRunnable = new Runnable() {
		@Override
		public void run() {
			if (conn.service != null) {
				conn.service.seek(seekStep);
				seekStep = seekStep + seekStep / 3;
				if (seekStep > 20000)
					seekStep = 20000;
				if (seekStep < -20000)
					seekStep = -20000;
			}
			handler.postDelayed(this, 200);
		}
	};

	public FilesSectionFragment() {
		super(APService.class, R.layout.fragment_files);
	}

	private long lastKeyUp;
	private boolean longClick;

	protected void prevClick() {
		if (System.currentTimeMillis() - lastKeyUp > 500)
			if (conn.service != null)
				conn.service.prev();
	}

	protected void prevLongClick() {
		seekStep = -1000;
		handler.postDelayed(seekRunnable, 200);
		longClick = true;
	}

	protected void touch(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			handler.removeCallbacks(seekRunnable);

			if (longClick)
				lastKeyUp = System.currentTimeMillis();
			longClick = false;
		}
	}

	protected void nextClick() {
		if (System.currentTimeMillis() - lastKeyUp > 500)
			if (conn.service != null)
				conn.service.next();
	}

	protected void nextLongClick() {
		seekStep = 1000;
		handler.postDelayed(seekRunnable, 200);
		longClick = true;
	}

	protected void playPause() {
		if (conn.service != null)
			conn.service.play_pause();
	}

	public void redo() {
		if (conn.service != null)
			conn.service.redo();
	}

	public void undo() {
		if (conn.service != null)
			conn.service.undo();
	}

	@Override
	protected void createUI(final IAPService service) {
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		rootView.findViewById(R.id.buttons).setVisibility(View.GONE);
	}

	@Override
	public void onPause() {
		handler.removeCallbacks(seekRunnable);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}
