package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.files.tape.LevelView;
import kvv.aplayer.files.tape.TapePanel;
import kvv.aplayer.files.tape.TapeView;
import kvv.aplayer.player.Player.OnChangedHint;
import kvv.aplayer.service.APServiceListener;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
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

import com.smartbean.androidutils.util.Utils;

public class FilesSectionFragmentList extends FilesSectionFragment implements
		APServiceListener {

	private static boolean tape;

	private ListView list;
	private TapePanel tapePanel;
	private View listPanel;
	private TapeView tapeView;
	private LevelView levelView;
	private LevelView levelView1;
	private TextView progressText;
	private Button pause;
	private Button timing;
	private ProgressBar fileProgressBar;
	protected TextView folderTextView;
	private ProgressBar folderProgressBar;

	private SharedPreferences settings;

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

	@Override
	protected void restartButtonsTimer() {
		handler.removeCallbacks(buttonsRunnable);
		handler.postDelayed(buttonsRunnable, APActivity.BUTTONS_DELAY);
	}

	private OnClickListener timingListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			restartButtonsTimer();
		}
	};

	@Override
	public void onChanged(OnChangedHint hint) {
		if (conn.service == null)
			return;

		switch (hint) {
		case FOLDER:
			folderChanged();
		case FILE:
			trackChanged();
		case POSITION:
			positionChanged();
		}
	}

	@Override
	public void onSpeedChanged(boolean hasSpeed, int fromLocation,
			int fromPlayer) {
	}

	@Override
	public void onLoaded() {
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void createUI(final IAPService service) {
		super.createUI(service);

		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		folderTextView = (TextView) rootView.findViewById(R.id.folder);
		fileProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
		folderProgressBar = (ProgressBar) rootView
				.findViewById(R.id.folderProgress);
		timing = (Button) rootView.findViewById(R.id.timing);
		timing.setOnClickListener(timingListener);

		list = (ListView) rootView.findViewById(R.id.list);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				rootView.findViewById(R.id.buttons).setVisibility(View.VISIBLE);
				restartButtonsTimer();
				FilesAdapter adapter = (FilesAdapter) list.getAdapter();
				if (adapter != null) {
					adapter.sel = position;
					list.invalidateViews();
				}
			}
		});

		final SoundPool soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		final int id = soundPool.load(getActivity(), R.raw.sw, 1);
		
		pause = (Button) rootView.findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playPause();
			}
		});

		pause.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				rootView.findViewById(R.id.undoPanel).setVisibility(
						View.VISIBLE);
				return true;
			}
		});

		rootView.findViewById(R.id.closeUndo).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						rootView.findViewById(R.id.undoPanel).setVisibility(
								View.GONE);
					}
				});

		rootView.findViewById(R.id.undo).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						undo();
					}
				});

		rootView.findViewById(R.id.redo).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						redo();
					}
				});

		OnTouchListener onTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchX = event.getX();
				touchY = event.getY();
				return false;
			}
		};

		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (conn.service != null) {
					int dur = conn.service.getDuration();
					int pos = (int) (dur * touchX / v.getWidth());
					System.out.println("seek to " + pos);
					conn.service.seekTo(pos);
				}
			}
		};

		progressText = (TextView) rootView.findViewById(R.id.progressText);
		progressText.setOnTouchListener(onTouchListener);
		progressText.setOnClickListener(onClickListener);

		fileProgressBar.setOnTouchListener(onTouchListener);
		fileProgressBar.setOnClickListener(onClickListener);

		tapePanel = (TapePanel) rootView.findViewById(R.id.tapePanel);
		listPanel = rootView.findViewById(R.id.listPanel);
		tapeView = (TapeView) rootView.findViewById(R.id.tape);

		levelView = (LevelView) rootView.findViewById(R.id.level);
		levelView.setScale(new int[] { -20, -10, -6, -3, 0, 3 });
		levelView1 = (LevelView) rootView.findViewById(R.id.level1);
		levelView1.setScale(new int[] { -20, -10, -6, -3, 0, 3 });

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

		folderTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tape = !tape;
				enDisViews();
			}
		});

		final Runnable seekRunnable = new Runnable() {
			@Override
			public void run() {
				if (tapeView != null) { 
					soundPool.play(id, 1, 1, 1, 0, 1);
					tapeView.setSeek(0);
				}
			}
		};


		
		tapeView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (conn.service == null)
					return;
				int ht = tapePanel.hitTest(touchX, touchY);
				switch (ht) {
				case -1:
					soundPool.play(id, 1, 1, 1, 0, 1);
					handler.removeCallbacks(seekRunnable);
					rotatePrev();
					handler.postDelayed(seekRunnable, 500);
					prevClick();
					break;
				case 1:
					soundPool.play(id, 1, 1, 1, 0, 1);
					if (conn.service.getFile() < conn.service.getFileCnt() - 1) {
						handler.removeCallbacks(seekRunnable);
						rotateNext();
					}
					handler.postDelayed(seekRunnable, 500);
					nextClick();
					break;
				case -2:
					undo();
					break;
				case 2:
					redo();
					break;
				default:
					soundPool.play(id, 1, 1, 1, 0, 1);
					playPause();
					break;
				}
			}

		});

		tapeView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				int ht = tapePanel.hitTest(touchX, touchY);
				if (ht == 1) {
					handler.removeCallbacks(seekRunnable);
					rotateNext();
					nextLongClick();
				} else if (ht == -1) {
					handler.removeCallbacks(seekRunnable);
					rotatePrev();
					prevLongClick();
				} else
					rootView.findViewById(R.id.undoPanel).setVisibility(
							View.VISIBLE);
				return true;
			}
		});

		tapeView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchX = event.getX();
				touchY = event.getY();
				touch(event);
				if (event.getAction() == MotionEvent.ACTION_UP) {
					seekRunnable.run();
				}
				return false;
			}
		});

		Button prev = (Button) rootView.findViewById(R.id.prev);
		prev.setOnClickListener(prevOnClickListener);
		prev.setOnLongClickListener(prevOnLongClickListener);
		prev.setOnTouchListener(prevOnTouchListener);

		Button next = (Button) rootView.findViewById(R.id.next);
		next.setOnClickListener(nextOnClickListener);
		next.setOnLongClickListener(nextOnLongClickListener);
		next.setOnTouchListener(nextOnTouchListener);

		enDisViews();

		service.addListener(this);
		onChanged(OnChangedHint.FOLDER);

		setMagicEye();
	}

	private void rotatePrev() {
		if (tapeView != null && tape)
			tapeView.setSeek(-2000);
	}

	private void rotateNext() {
		if (tapeView != null && tape)
			tapeView.setSeek(2000);
	}

	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(this);
		super.onDestroy();
	}

	private float touchX;
	private float touchY;

	private void clearButtons() {
		System.out.println("clearButtons()");
		handler.removeCallbacks(buttonsRunnable);
		// if (!settings.getBoolean(getString(R.string.prefTestMode), false))
		if (rootView != null) {
			rootView.findViewById(R.id.buttons).setVisibility(View.GONE);
		}
		FilesAdapter adapter = (FilesAdapter) list.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			list.invalidateViews();
		}
	}

	private long[] folderFilesStartPos;
	private long folderMax;

	private void folderChanged() {
		System.out.println("folderChanged()");
		FilesAdapter adapter = new FilesAdapter(getActivity(), conn.service);
		list.setAdapter(adapter);
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
		list.invalidateViews();
		list.setSelection(conn.service.getFile() - 2);
		int file = conn.service.getFile();
		FileDescriptor[] files = conn.service.getFiles();
		if (files.length > 0)
			progressText.setText(files[file].name);
	}

	private void positionChanged() {
		if (conn.service != null) {
			pause.setText(conn.service.isPlaying() ? "Pause" : "Play");

			int dur = conn.service.getDuration();
			int pos = conn.service.getCurrentPosition();

			fileProgressBar.setMax(dur);
			fileProgressBar.setProgress(pos);
			timing.setText(Utils.convertSecondsToHMmSs(pos / 1000) + "("
					+ Utils.convertSecondsToHMmSs(dur / 1000) + ")");

			int file = conn.service.getFile();
			FileDescriptor[] files = conn.service.getFiles();
			if (files.length > 0 && folderFilesStartPos != null
					&& folderFilesStartPos.length > file) {
				int max = (int) (folderMax / 1000);
				int cur = (int) (folderFilesStartPos[file] + pos) / 1000;

				tapeView.setProgress(max, cur);
				folderProgressBar.setMax(max);
				folderProgressBar.setProgress(cur);
			}

			updateTapeViewState();
		}
	}

	private void updateTapeViewState() {
		if (tapeView == null)
			return;

		if (tape && fg && conn.service != null && conn.service.isPlaying()) {
			tapeView.start();
		} else {
			tapeView.stop();
		}
	}

	private void enDisViews() {
		if (tape) {
			tapePanel.setVisibility(View.VISIBLE);
			listPanel.setVisibility(View.GONE);
			progressText.setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.bottomButtons).setVisibility(View.GONE);
		} else {
			tapePanel.setVisibility(View.GONE);
			listPanel.setVisibility(View.VISIBLE);
			progressText.setVisibility(View.GONE);
			rootView.findViewById(R.id.bottomButtons).setVisibility(
					View.VISIBLE);
		}

		updateTapeViewState();

		if (conn.service != null)
			conn.service.setVisible(tape);
	}

	private boolean fg;

	@Override
	public void onPause() {
		fg = false;
		if (conn.service != null)
			conn.service.setVisible(false);
		handler.removeCallbacks(progressRunnable);
		updateTapeViewState();
		super.onPause();
	}

	public void onResume() {
		super.onResume();
		progressRunnable.run();
		fg = true;
		if (conn.service != null)
			conn.service.setVisible(tape);
		updateTapeViewState();
		setMagicEye();
	}

	private OnClickListener prevOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			prevClick();
		}
	};

	private OnLongClickListener prevOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			prevLongClick();
			return true;
		}
	};

	@SuppressLint("ClickableViewAccessibility")
	private OnTouchListener prevOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			touch(event);
			return false;
		}
	};

	private OnClickListener nextOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			nextClick();
		}
	};

	private OnLongClickListener nextOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			nextLongClick();
			return true;
		}
	};

	@SuppressLint("ClickableViewAccessibility")
	private OnTouchListener nextOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			touch(event);
			return false;
		}
	};

	@Override
	public void onLevelChanged(float level) {
		if (levelView != null && tape)
			levelView.setLevel(level);
		if (levelView1 != null && tape)
			levelView1.setLevel(level);
	}

	public void setMagicEye() {
		if(settings == null)
			return;
		boolean magicEye = settings.getBoolean(getString(R.string.prefMagicEye), false);

		levelView.setVisibility(magicEye ? View.GONE : View.VISIBLE);
		levelView1.setVisibility(magicEye ? View.VISIBLE : View.GONE);
	}

}
