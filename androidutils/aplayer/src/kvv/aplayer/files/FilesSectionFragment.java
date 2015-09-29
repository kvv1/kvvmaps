package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.IAPService;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.smartbean.androidutils.fragment.RLFragment;

public class FilesSectionFragment extends RLFragment<APActivity, IAPService> {

	SharedPreferences settings;

	protected Handler handler = new Handler();

	protected void positionChanged() {
	}

	protected void restartButtonsTimer() {
	}

	private Runnable progressRunnable = new Runnable() {
		@Override
		public void run() {
			positionChanged();
			handler.removeCallbacks(this);
			handler.postDelayed(this, 1000);
		}
	};

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

	private void updateExtButtons() {
		if (conn.service == null)
			return;

		((Button) rootView.findViewById(R.id.speedOn))
				.setText((int) APService.dBPer100kmh[conn.service
						.getDBPer100Idx()] + " db/100kmh");

		((Button) rootView.findViewById(R.id.comprOn)).setText("compr "
				+ APService.compr[conn.service.getComprIdx()] + " db");

		((Button) rootView.findViewById(R.id.vol0)).setTypeface(null,
				conn.service.getGain() == 0 ? Typeface.BOLD : Typeface.NORMAL);

		((Button) rootView.findViewById(R.id.volPlus1)).setTypeface(null,
				conn.service.getGain() == 5 ? Typeface.BOLD : Typeface.NORMAL);

		((Button) rootView.findViewById(R.id.volPlus2)).setTypeface(null,
				conn.service.getGain() == 10 ? Typeface.BOLD : Typeface.NORMAL);

		// System.out.println("level = " + conn.service.getLevel());

	}

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

		if (!settings.getBoolean(getString(R.string.prefTestMode), false))
			rootView.findViewById(R.id.extButtons).setVisibility(View.GONE);

		((Button) rootView.findViewById(R.id.speedOn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						int idx = (service.getDBPer100Idx() + 1)
								% APService.dBPer100kmh.length;
						service.setDBPer100Idx(idx);
						restartButtonsTimer();
						updateExtButtons();
					}
				});

		((Button) rootView.findViewById(R.id.comprOn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						int idx = (service.getComprIdx() + 1)
								% APService.compr.length;
						service.setComprIdx(idx);
						restartButtonsTimer();
						updateExtButtons();
					}
				});

		((Button) rootView.findViewById(R.id.vol0))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(0);
						restartButtonsTimer();
						updateExtButtons();
					}
				});

		((Button) rootView.findViewById(R.id.volPlus1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(5);
						restartButtonsTimer();
						updateExtButtons();
					}
				});

		((Button) rootView.findViewById(R.id.volPlus2))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(10);
						restartButtonsTimer();
						updateExtButtons();
					}
				});

		((Button) rootView.findViewById(R.id.back10s))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.seek(-10000);
						restartButtonsTimer();
					}
				});

		((Button) rootView.findViewById(R.id.back30s))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.seek(-30000);
						restartButtonsTimer();
					}
				});

		((Button) rootView.findViewById(R.id.back1min))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.seek(-60000);
						restartButtonsTimer();
					}
				});

		updateExtButtons();

	}

	@Override
	public void onPause() {
		handler.removeCallbacks(progressRunnable);
		handler.removeCallbacks(seekRunnable);
		super.onPause();
	}

	@Override
	public void onResume() {
		progressRunnable.run();
		super.onResume();
	}

}
