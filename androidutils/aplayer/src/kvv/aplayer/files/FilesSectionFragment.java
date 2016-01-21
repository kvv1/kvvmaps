package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.files.tape.LevelView;
import kvv.aplayer.files.tape.TapePanel;
import kvv.aplayer.files.tape.TapeView;
import kvv.aplayer.player.Player.OnChangedHint;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.APServiceListener;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartbean.androidutils.fragment.FragmentX;
import com.smartbean.androidutils.util.Utils;

public class FilesSectionFragment extends FragmentX<APActivity, IAPService>
		implements APServiceListener {

	public FilesSectionFragment() {
		super(APService.class, R.layout.fragment_files);
	}

	private static boolean tape;

	private ListView listView;
	private TapePanel tapePanel;
	private View listPanel;
	private TapeView tapeView;
	private LevelView arrowLevelView;
	private LevelView magicEyeLevelView;
	private TextView progressText;
	private Button pause;
	private ProgressBar fileProgressBar;
	protected TextView folderTextView;
	private ProgressBar folderProgressBar;

	private SharedPreferences settings;
	protected Handler handler = new Handler();

	private int seekStep = 0;

	private Runnable progressRunnable = new Runnable() {
		@Override
		public void run() {
			positionChanged();
			handler.removeCallbacks(this);
			handler.postDelayed(this, 1000);
		}
	};

	private Runnable buttonsRunnable = new Runnable() {
		@Override
		public void run() {
			clearButtons();
		}
	};

	private Runnable undoRunnable = new Runnable() {
		@Override
		public void run() {
			rootView.findViewById(R.id.undoPanel).setVisibility(View.GONE);
		}
	};

	private void restartButtonsTimer() {
		rootView.findViewById(R.id.goto1).setVisibility(View.VISIBLE);
		handler.removeCallbacks(buttonsRunnable);
		handler.postDelayed(buttonsRunnable, APActivity.BUTTONS_DELAY);
	}

	@Override
	public void onUndoAdded() {
		if (rootView == null)
			return;
		rootView.findViewById(R.id.undoPanel).setVisibility(View.VISIBLE);
		handler.removeCallbacks(undoRunnable);
		handler.postDelayed(undoRunnable, 5000);
	}

	@Override
	public void onChanged(OnChangedHint hint) {
		if (conn.service == null)
			return;

		switch (hint) {
		case FOLDER:
			folderChanged();
		case FILE:
			trackChanged();
		case STATE:
			stateChanged();
		}
	}

	@Override
	public void onLoaded() {
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void createUI(final IAPService service) {
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		rootView.findViewById(R.id.goto1).setVisibility(View.GONE);

		folderTextView = (TextView) rootView.findViewById(R.id.folder);
		fileProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
		folderProgressBar = (ProgressBar) rootView
				.findViewById(R.id.folderProgress);

		listView = (ListView) rootView.findViewById(R.id.list);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				restartButtonsTimer();
				FilesAdapter adapter = (FilesAdapter) listView.getAdapter();
				if (adapter != null) {
					adapter.sel = position;
					listView.invalidateViews();
				}
			}
		});

		undoRunnable.run();

		pause = (Button) rootView.findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (conn.service != null)
					conn.service.play_pause();
			}
		});

		pause.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				onUndoAdded();
				return true;
			}
		});

		rootView.findViewById(R.id.undo).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (conn.service != null)
							conn.service.undo();
					}
				});

		rootView.findViewById(R.id.redo).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (conn.service != null)
							conn.service.redo();
					}
				});

		progressText = (TextView) rootView.findViewById(R.id.progressText);

		new TouchListener(progressText) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service != null) {
					int dur = conn.service.getDuration();
					int pos = (int) (dur * touchX / progressText.getWidth());
					System.out.println("seek to " + pos);
					conn.service.seekTo(pos);
				}
			}
		};

		tapePanel = (TapePanel) rootView.findViewById(R.id.tapePanel);
		listPanel = rootView.findViewById(R.id.listPanel);
		tapeView = (TapeView) rootView.findViewById(R.id.tape);

		arrowLevelView = (LevelView) rootView.findViewById(R.id.arrowLevel);
		arrowLevelView.setScale(new int[] { -20, -10, -6, -3, 0, 3 });
		magicEyeLevelView = (LevelView) rootView
				.findViewById(R.id.magicEyeLevel);
		magicEyeLevelView.setScale(new int[] { -20, -10, -6, -3, 0, 3 });

		((Button) rootView.findViewById(R.id.goto1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						FilesAdapter adapter = (FilesAdapter) listView
								.getAdapter();
						if (adapter != null && adapter.sel >= 0
								&& conn.service != null) {
							conn.service.toFile(adapter.sel);
						}
					}
				});

		folderTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tape = !tape;
				setCurrentSkin();
			}
		});

		new TouchListener(tapeView) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service == null)
					return;
				int ht = tapeView.hitTest(touchX, touchY);
				if (ht == -1) {
					tapeView.setSeek(-2000, true);
					conn.service.prev();
				} else if (ht == 1) {
					if (conn.service.getFile() < conn.service.getFileCnt() - 1)
						tapeView.setSeek(2000, true);
					conn.service.next();
				} else {
					conn.service.play_pause();
				}
			}

			@Override
			protected void onLongClick(float touchX, float touchY) {
				int ht = tapeView.hitTest(touchX, touchY);
				if (ht == -1) {
					tapeView.setSeek(-2000, false);
					seekStep = -1000;
				} else if (ht == 1) {
					tapeView.setSeek(2000, false);
					seekStep = 1000;
				} else
					onUndoAdded();
			}

			@Override
			protected void onHold(float touchX, float touchY) {
				FilesSectionFragment.this.onHold();
			}

			@Override
			protected void onReleased(float touchX, float touchY) {
				if (tapeView != null)
					tapeView.setSeek(0, false);
			}
		};

		Button prev = (Button) rootView.findViewById(R.id.prev);

		new TouchListener(prev) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service != null)
					conn.service.prev();
			}

			@Override
			protected void onLongClick(float touchX, float touchY) {
				seekStep = -1000;
			}

			@Override
			protected void onHold(float touchX, float touchY) {
				FilesSectionFragment.this.onHold();
			}
		};

		Button next = (Button) rootView.findViewById(R.id.next);

		new TouchListener(next) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service != null)
					conn.service.next();
			}

			@Override
			protected void onLongClick(float touchX, float touchY) {
				seekStep = 1000;
			}

			@Override
			protected void onHold(float touchX, float touchY) {
				FilesSectionFragment.this.onHold();
			}
		};

		setCurrentSkin();

		service.addListener(this);
		onChanged(OnChangedHint.FOLDER);

		setMagicEye();
	}

	private void onHold() {
		if (conn.service != null) {
			conn.service.seek(seekStep);
			seekStep = seekStep + seekStep / 3;
			if (seekStep > 20000)
				seekStep = 20000;
			if (seekStep < -20000)
				seekStep = -20000;
		}
	}

	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(this);
		super.onDestroy();
	}

	private void clearButtons() {
		System.out.println("clearButtons()");
		handler.removeCallbacks(buttonsRunnable);
		if (rootView != null) {
			rootView.findViewById(R.id.goto1).setVisibility(View.GONE);
		}
		FilesAdapter adapter = (FilesAdapter) listView.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			listView.invalidateViews();
		}
	}

	private long[] folderFilesStartPos;
	private long folderMax;

	private void folderChanged() {
		System.out.println("folderChanged()");
		FilesAdapter adapter = new FilesAdapter(getActivity(), conn.service);
		listView.setAdapter(adapter);
		folderTextView.setText(conn.service.getFolders().get(
				conn.service.getCurrentFolder()).displayName);

		FileDescriptor[] files = conn.service.getFiles();

		folderMax = 0;
		folderFilesStartPos = new long[files.length];
		for (int i = 0; i < files.length; i++) {
			FileDescriptor file = files[i];
			folderFilesStartPos[i] = folderMax;
			folderMax += file.duration;
		}

	}

	private void trackChanged() {
		System.out.println("trackChanged()");
		clearButtons();
		listView.invalidateViews();
		listView.setSelection(conn.service.getFile() - 2);
		int file = conn.service.getFile();
		FileDescriptor[] files = conn.service.getFiles();
		if (files.length > 0)
			progressText.setText(files[file].name);
	}

	private void stateChanged() {
		// if (conn.service != null)
		// pause.setText(conn.service.isPlaying() ? "Pause" : "Play");

		updateTapeViewState();
		positionChanged();
	}

	private void positionChanged() {
		if (conn.service != null) {

			int dur = conn.service.getDuration();
			int pos = conn.service.getCurrentPosition();

			fileProgressBar.setMax(dur);
			fileProgressBar.setProgress(pos);
			if (!tape) {
				String time = Utils.convertSecondsToHMmSs(pos / 1000) + "("
						+ Utils.convertSecondsToHMmSs(dur / 1000) + ")";
				pause.setText((conn.service.isPlaying() ? "Pause" : "Play")
						+ "  " + time);
				// timing.setText(Utils.convertSecondsToHMmSs(pos / 1000) + "("
				// + Utils.convertSecondsToHMmSs(dur / 1000) + ")");
			}

			int file = conn.service.getFile();
			FileDescriptor[] files = conn.service.getFiles();
			if (files.length > 0 && folderFilesStartPos != null
					&& folderFilesStartPos.length > file) {
				int max = (int) (folderMax / 1000);
				int cur = (int) (folderFilesStartPos[file] + pos) / 1000;

				if (tape)
					tapeView.setProgress(max, cur);
				folderProgressBar.setMax(max);
				folderProgressBar.setProgress(cur);
			}
		}
	}

	private void updateTapeViewState() {
		if (tapeView == null)
			return;

		if (tape && isResumed() && conn.service != null
				&& conn.service.isPlaying()) {
			tapeView.start();
		} else {
			tapeView.stop();
		}
	}

	private void setCurrentSkin() {
		if (tape) {
			tapePanel.setVisibility(View.VISIBLE);
			listPanel.setVisibility(View.GONE);
			rootView.findViewById(R.id.bottomButtons).setVisibility(View.GONE);
		} else {
			tapePanel.setVisibility(View.GONE);
			listPanel.setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.bottomButtons).setVisibility(
					View.VISIBLE);
		}

		updateTapeViewState();

		if (conn.service != null)
			conn.service.setVisible(tape);
	}

	@Override
	public void onPause() {
		if (conn.service != null)
			conn.service.setVisible(false);
		handler.removeCallbacks(progressRunnable);
		updateTapeViewState();
		super.onPause();
	}

	public void onResume() {
		super.onResume();
		progressRunnable.run();
		if (conn.service != null)
			conn.service.setVisible(tape);
		updateTapeViewState();
		setMagicEye();
	}

	@Override
	public void onLevelChanged(float level) {
		if (arrowLevelView != null && tape)
			arrowLevelView.setLevel(level);
		if (magicEyeLevelView != null && tape)
			magicEyeLevelView.setLevel(level);
	}

	public void setMagicEye() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (settings == null)
					return;
				boolean magicEye = settings.getBoolean(
						getString(R.string.prefMagicEye), false);

				arrowLevelView.setVisibility(magicEye ? View.GONE
						: View.VISIBLE);
				magicEyeLevelView.setVisibility(magicEye ? View.VISIBLE
						: View.GONE);

				if (tapeView != null)
					tapeView.click = settings.getBoolean(
							getString(R.string.prefClick), false);
			}
		}, 500);

	}

}
