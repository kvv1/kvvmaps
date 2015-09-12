package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.folders.Folder;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.APServiceListener;
import kvv.aplayer.service.APServiceListenerAdapter;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartbean.androidutils.fragment.RLFragment;

public class FilesSectionFragment extends RLFragment<APActivity, IAPService> {

	SharedPreferences settings;

	protected Handler handler = new Handler();

	private Button timing;
	private ProgressBar progressBar;
	protected TextView folderTextView;

	protected void folderChanged() {

	}

	protected void trackChanged() {

	}

	protected void restartButtonsTimer() {
	}

	protected void seekStart(int step) {
	}

	protected void seekEnd() {

	}

	private Runnable progressRunnable = new Runnable() {
		@Override
		public void run() {
			updateUI();
			handler.removeCallbacks(this);
			handler.postDelayed(this, 1000);
		}
	};

	private int seekStep = 5000;

	private Runnable seekForwardRunnable = new Runnable() {
		@Override
		public void run() {
			if (conn.service != null) {
				seekStart(seekStep);
				conn.service.seek(seekStep);
				seekStep = seekStep + seekStep / 3;
				if (seekStep > 20000)
					seekStep = 20000;
			}
			updateUI();
			handler.postDelayed(this, 200);
		}
	};

	private Runnable seekBackRunnable = new Runnable() {
		@Override
		public void run() {
			if (conn.service != null) {
				seekStart(-seekStep);
				conn.service.seek(-seekStep);
				seekStep = seekStep + seekStep / 3;
				if (seekStep > 20000)
					seekStep = 20000;
			}
			updateUI();
			handler.postDelayed(this, 200);
		}
	};

	@SuppressLint("DefaultLocale")
	private static String convertSecondsToHMmSs(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;

		if (h != 0)
			return String.format("%d:%02d:%02d", h, m, s);
		return String.format("%02d:%02d", m, s);
	}

	protected void updateUI() {
		if (conn.service == null)
			return;

		int dur = conn.service.getDuration();
		int pos = conn.service.getCurrentPosition();

		progressBar.setMax(dur);
		progressBar.setProgress(pos);

		timing.setText(convertSecondsToHMmSs(pos / 1000) + "("
				+ convertSecondsToHMmSs(dur / 1000) + ")");

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

//		System.out.println("level = " + conn.service.getLevel());

	}

	private final APServiceListener listener = new APServiceListenerAdapter() {
		@Override
		public void onChanged() {
			if (conn.service == null)
				return;

			if (conn.service.getFolders().size() > 0) {
				if (conn.service.getCurrentFolder() != folder
						&& conn.service.getFolders().size() > 0) {
					folderChanged();
					folder = conn.service.getCurrentFolder();
				}
				if (folder >= 0) {
					Folder fold = conn.service.getFolders().get(folder);
					folderTextView.setText(fold.displayName);
				}
			}

			trackChanged();
			updateUI();
		}

		@Override
		public void onRandomChanged() {
			folder = -1;
			onChanged();
		}
	};

	private int folder;

	public FilesSectionFragment() {
		super(APService.class, R.layout.fragment_files);
	}

	protected void prevClick() {
		if (System.currentTimeMillis() - lastKeyUp > 500)
			onPrev();
	}

	protected void prevLongClick() {
		seekStep = 1000;
		handler.postDelayed(seekBackRunnable, 200);
		longClick = true;
	}

	protected void touch(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP)
			keyUp();
	}

	protected void nextClick() {
		if (System.currentTimeMillis() - lastKeyUp > 500)
			onNext();
	}

	protected void nextLongClick() {
		seekStep = 1000;
		handler.postDelayed(seekForwardRunnable, 200);
		longClick = true;
	}

	protected void playPause() {
		if (conn.service != null)
			conn.service.play_pause();
		updateUI();
	}

	public void redo() {
		if (conn.service != null)
			conn.service.redo();
		updateUI();
	}

	public void undo() {
		if (conn.service != null)
			conn.service.undo();
		updateUI();
	}

	
	@Override
	protected void createUI(final IAPService service) {
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		folder = -1;
		rootView.findViewById(R.id.buttons).setVisibility(View.GONE);

		folderTextView = (TextView) rootView.findViewById(R.id.folder);

		if (!settings.getBoolean(getString(R.string.prefTestMode), false))
			rootView.findViewById(R.id.extButtons).setVisibility(View.GONE);


		timing = (Button) rootView.findViewById(R.id.timing);

		final View extButtons = rootView.findViewById(R.id.extButtons);

		OnClickListener timingListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				extButtons.setVisibility(View.VISIBLE);
				restartButtonsTimer();
			}
		};

		timing.setOnClickListener(timingListener);
		rootView.findViewById(R.id.level).setOnClickListener(timingListener);

		((Button) rootView.findViewById(R.id.speedOn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						int idx = (service.getDBPer100Idx() + 1)
								% APService.dBPer100kmh.length;
						service.setDBPer100Idx(idx);
						restartButtonsTimer();
						updateUI();
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
						updateUI();
					}
				});

		((Button) rootView.findViewById(R.id.vol0))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(0);
						restartButtonsTimer();
						updateUI();
					}
				});

		((Button) rootView.findViewById(R.id.volPlus1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(5);
						restartButtonsTimer();
						updateUI();
					}
				});

		((Button) rootView.findViewById(R.id.volPlus2))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(10);
						restartButtonsTimer();
						updateUI();
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

		progressBar = (ProgressBar) rootView.findViewById(R.id.progress);

		service.addListener(listener);

		handler.post(new Runnable() {
			@Override
			public void run() {
				listener.onChanged();
			}
		});
	}

	private long lastKeyUp;
	private boolean longClick;

	private void keyUp() {
		handler.removeCallbacks(seekForwardRunnable);
		handler.removeCallbacks(seekBackRunnable);
		seekEnd();

		if (longClick)
			lastKeyUp = System.currentTimeMillis();
		longClick = false;
	}

	@Override
	public void onPause() {
		handler.removeCallbacks(progressRunnable);
		handler.removeCallbacks(seekForwardRunnable);
		handler.removeCallbacks(seekBackRunnable);
		seekEnd();
		super.onPause();
	}

	@Override
	public void onResume() {
		progressRunnable.run();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
		super.onDestroy();
	}

	protected void onNext() {
		if (conn.service != null)
			conn.service.next();
	}

	protected void onPrev() {
		if (conn.service != null)
			conn.service.prev();
	}
}
