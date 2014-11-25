package kvv.aplayer;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartbean.androidutils.fragment.RLFragment;

public class FilesSectionFragment extends RLFragment<APActivity, IAPService> {

	SharedPreferences settings;

	private Handler handler = new Handler();

	private Button pause;
	private Button timing;
	private ProgressBar progressBar;

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
				conn.service.seek(-seekStep);
				seekStep = seekStep + seekStep / 3;
				if (seekStep > 20000)
					seekStep = 20000;
			}
			updateUI();
			handler.postDelayed(this, 200);
		}
	};

	private static String convertSecondsToHMmSs(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;

		if (h != 0)
			return String.format("%d:%02d:%02d", h, m, s);
		return String.format("%02d:%02d", m, s);
	}

	private void updateUI() {
		if (conn.service == null)
			return;

		int dur = conn.service.getDuration();
		int pos = conn.service.getCurrentPosition();

		progressBar.setMax(dur);
		progressBar.setProgress(pos);

		pause.setText(conn.service.isPlaying() ? "Pause" : "Play");

		timing.setText(convertSecondsToHMmSs(pos / 1000) + "("
				+ convertSecondsToHMmSs(dur / 1000) + ")");

		((Button) rootView.findViewById(R.id.comprOff)).setTypeface(null,
				conn.service.getCompr() ? Typeface.NORMAL : Typeface.BOLD);

		((Button) rootView.findViewById(R.id.comprOn)).setTypeface(null,
				conn.service.getCompr() ? Typeface.BOLD : Typeface.NORMAL);

		((Button) rootView.findViewById(R.id.vol0)).setTypeface(null,
				conn.service.getGain() == 0 ? Typeface.BOLD : Typeface.NORMAL);

		((Button) rootView.findViewById(R.id.volPlus1)).setTypeface(null,
				conn.service.getGain() == 5 ? Typeface.BOLD : Typeface.NORMAL);

		((Button) rootView.findViewById(R.id.volPlus2)).setTypeface(null,
				conn.service.getGain() == 10 ? Typeface.BOLD : Typeface.NORMAL);

	}

	private final APServiceListener listener = new APServiceListener() {
		@Override
		public void onChanged() {
			if (conn.service == null)
				return;

			if (conn.service.getFolders().size() > 0) {
				if (conn.service.getCurrentFolder() != folder
						&& conn.service.getFolders().size() > 0) {
					FilesAdapter adapter = new FilesAdapter(getActivity(),
							conn.service);
					list.setAdapter(adapter);
					folder = conn.service.getCurrentFolder();
				}
				if (folder >= 0) {
					Folder fold = conn.service.getFolders().get(folder);
					TextView folderTextView = (TextView) rootView
							.findViewById(R.id.folder);
					folderTextView.setText(fold.displayName + " ("
							+ (conn.service.getFile() + 1) + "/"
							+ fold.files.length + ")");
				}
			}

			clearGoto();

			list.invalidateViews();
			list.setSelection(conn.service.getFile() - 2);

			updateUI();
		}

		@Override
		public void onBookmarksChanged() {
		}

		@Override
		public void onRandomChanged() {
			folder = -1;
			onChanged();
		}
	};

	private ListView list;
	private int folder;

	public FilesSectionFragment() {
		super(APService.class);
	}

	@Override
	protected int getLayout() {
		return R.layout.fragment_files;
	}

	private Runnable gotoRunnable = new Runnable() {
		@Override
		public void run() {
			if (rootView != null) {
				clearGoto();
			}
		}
	};

	private void clearGoto() {
		handler.removeCallbacks(gotoRunnable);
		if (!settings.getBoolean(getString(R.string.prefTestMode), false))
			rootView.findViewById(R.id.extButtons).setVisibility(View.GONE);
		rootView.findViewById(R.id.buttons).setVisibility(View.GONE);
		FilesAdapter adapter = (FilesAdapter) list.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			list.invalidateViews();
		}
	}

	@Override
	protected void createUI(final IAPService service) {
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		folder = -1;
		list = (ListView) rootView.findViewById(R.id.list);
		rootView.findViewById(R.id.buttons).setVisibility(View.GONE);

		if (!settings.getBoolean(getString(R.string.prefTestMode), false))
			rootView.findViewById(R.id.extButtons).setVisibility(View.GONE);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				rootView.findViewById(R.id.buttons).setVisibility(View.VISIBLE);
				handler.removeCallbacks(gotoRunnable);
				handler.postDelayed(gotoRunnable, APActivity.BUTTONS_DELAY);
				FilesAdapter adapter = (FilesAdapter) list.getAdapter();
				if (adapter != null) {
					adapter.sel = position;
					list.invalidateViews();
				}
			}
		});

		((Button) rootView.findViewById(R.id.goto1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						FilesAdapter adapter = (FilesAdapter) list.getAdapter();
						if (adapter != null && adapter.sel >= 0
								&& conn.service != null) {
							conn.service.toFile(adapter.sel);
						}
					}
				});

		Button prev = (Button) rootView.findViewById(R.id.prev);

		prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (System.currentTimeMillis() - lastKeyUp > 500)
					service.prev();
			}
		});

		prev.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				seekStep = 1000;
				handler.postDelayed(seekBackRunnable, 200);
				longClick = true;
				return true;
			}
		});

		prev.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP)
					keyUp();
				return false;
			}
		});

		Button next = (Button) rootView.findViewById(R.id.next);

		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (System.currentTimeMillis() > 500)
					service.next();
			}
		});

		next.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				seekStep = 1000;
				handler.postDelayed(seekForwardRunnable, 200);
				longClick = true;
				return true;
			}
		});

		next.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP)
					keyUp();
				return false;
			}
		});

		pause = (Button) rootView.findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				service.play_pause();
				updateUI();
			}
		});

		timing = (Button) rootView.findViewById(R.id.timing);

		final View extButtons = rootView.findViewById(R.id.extButtons);

		timing.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				extButtons.setVisibility(View.VISIBLE);
				handler.removeCallbacks(gotoRunnable);
				handler.postDelayed(gotoRunnable, APActivity.BUTTONS_DELAY);
			}
		});

		((Button) rootView.findViewById(R.id.comprOff))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setCompr(false);
						handler.removeCallbacks(gotoRunnable);
						handler.postDelayed(gotoRunnable,
								APActivity.BUTTONS_DELAY);
						updateUI();
					}
				});

		((Button) rootView.findViewById(R.id.comprOn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setCompr(true);
						handler.removeCallbacks(gotoRunnable);
						handler.postDelayed(gotoRunnable,
								APActivity.BUTTONS_DELAY);
						updateUI();
					}
				});

		((Button) rootView.findViewById(R.id.vol0))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(0);
						handler.removeCallbacks(gotoRunnable);
						handler.postDelayed(gotoRunnable,
								APActivity.BUTTONS_DELAY);
						updateUI();
					}
				});

		((Button) rootView.findViewById(R.id.volPlus1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(5);
						handler.removeCallbacks(gotoRunnable);
						handler.postDelayed(gotoRunnable,
								APActivity.BUTTONS_DELAY);
						updateUI();
					}
				});

		((Button) rootView.findViewById(R.id.volPlus2))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						service.setGain(10);
						handler.removeCallbacks(gotoRunnable);
						handler.postDelayed(gotoRunnable,
								APActivity.BUTTONS_DELAY);
						updateUI();
					}
				});

		((Button) rootView.findViewById(R.id.back10s))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						service.seek(-10000);
						handler.removeCallbacks(gotoRunnable);
						handler.postDelayed(gotoRunnable,
								APActivity.BUTTONS_DELAY);
					}
				});

		((Button) rootView.findViewById(R.id.back30s))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						service.seek(-30000);
						handler.removeCallbacks(gotoRunnable);
						handler.postDelayed(gotoRunnable,
								APActivity.BUTTONS_DELAY);
					}
				});

		((Button) rootView.findViewById(R.id.back1min))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						service.seek(-60000);
						handler.removeCallbacks(gotoRunnable);
						handler.postDelayed(gotoRunnable,
								APActivity.BUTTONS_DELAY);
					}
				});

		progressBar = (ProgressBar) rootView.findViewById(R.id.progress);

		service.addListener(listener);
		listener.onChanged();
	}

	private long lastKeyUp;
	private boolean longClick;

	private void keyUp() {
		handler.removeCallbacks(seekForwardRunnable);
		handler.removeCallbacks(seekBackRunnable);

		if (longClick)
			lastKeyUp = System.currentTimeMillis();
		longClick = false;
	}

	@Override
	public void onPause() {
		handler.removeCallbacks(progressRunnable);
		handler.removeCallbacks(seekForwardRunnable);
		handler.removeCallbacks(seekBackRunnable);
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

}
