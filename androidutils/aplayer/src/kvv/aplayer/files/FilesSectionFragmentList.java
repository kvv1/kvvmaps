package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.player.Player.OnChangedHint;
import kvv.aplayer.service.APServiceListener;
import kvv.aplayer.service.File1;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
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
	private View tapePanel;
	private View listPanel;
	private TapeView tapeView;
	private LevelView levelView;
	private TextView progressText;
	private Button pause;
	private Button timing;
	private ProgressBar fileProgressBar;
	protected TextView folderTextView;
	private View extButtons;

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
			extButtons.setVisibility(View.VISIBLE);
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

		folderTextView = (TextView) rootView.findViewById(R.id.folder);
		fileProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
		timing = (Button) rootView.findViewById(R.id.timing);
		timing.setOnClickListener(timingListener);

		extButtons = rootView.findViewById(R.id.extButtons);

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

		progressText = (TextView) rootView.findViewById(R.id.progressText);
		tapePanel = rootView.findViewById(R.id.tapePanel);
		listPanel = rootView.findViewById(R.id.listPanel);
		tapeView = (TapeView) rootView.findViewById(R.id.tape);
		levelView = (LevelView) rootView.findViewById(R.id.level);
		levelView.setOnClickListener(timingListener);

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

		tapeView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (conn.service == null)
					return;
				int ht = tapeView.hitTest(touchX, touchY);
				switch (ht) {
				case -1:
					rotatePrev();
					handler.postDelayed(seekRunnable, 500);
					prevClick();
					break;
				case 1:
					if (conn.service.getFile() < conn.service.getFileCnt() - 1)
						rotateNext();
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
					playPause();
					break;
				}
			}

		});

		tapeView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				int ht = tapeView.hitTest(touchX, touchY);
				if (ht == 1) {
					rotateNext();
					nextLongClick();
				} else if (ht == -1) {
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

		ViewTreeObserver vto = rootView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				rootView.getViewTreeObserver().removeGlobalOnLayoutListener(
						this);
				int width = rootView.getMeasuredWidth();
				View view_instance = levelView;
				android.view.ViewGroup.LayoutParams params = view_instance
						.getLayoutParams();
				int newLayoutWidth = (int) (width * 0.19);
				params.width = newLayoutWidth;
				params.height = newLayoutWidth;
				view_instance.setLayoutParams(params);// levelView.set
			}
		});

		enDisViews();

		service.addListener(this);
		onChanged(OnChangedHint.FOLDER);
	}

	private Runnable seekRunnable = new Runnable() {
		@Override
		public void run() {
			if (tapeView != null)
				tapeView.setSeek(0);
		}
	};

	private void rotatePrev() {
		if (tapeView != null && tape)
			tapeView.setSeek(-2000);
		handler.removeCallbacks(seekRunnable);
	}

	private void rotateNext() {
		if (tapeView != null && tape)
			tapeView.setSeek(2000);
		handler.removeCallbacks(seekRunnable);
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
			rootView.findViewById(R.id.extButtons).setVisibility(View.GONE);
			rootView.findViewById(R.id.buttons).setVisibility(View.GONE);
		}
		FilesAdapter adapter = (FilesAdapter) list.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			list.invalidateViews();
		}
	}

	private void folderChanged() {
		System.out.println("folderChanged()");
		FilesAdapter adapter = new FilesAdapter(getActivity(), conn.service);
		list.setAdapter(adapter);
		folderTextView.setText(conn.service.getFolders().get(
				conn.service.getCurrentFolder()).displayName);
	}

	private void trackChanged() {
		System.out.println("trackChanged()");
		clearButtons();
		list.invalidateViews();
		list.setSelection(conn.service.getFile() - 2);
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
			File1[] files = conn.service.getFiles();
			if (files.length > 0) {
				progressText.setText(files[file].name);
				if (dur > 0) {
					int max = 0;
					int cur = 0;
					for (int i = 0; i < files.length; i++) {
						File1 file1 = files[i];
						max += file1.duration / 1000;
						if (i < file)
							cur += file1.duration / 1000;
						if (i == file)
							cur += pos / 1000;
					}

					tapeView.setProgress(max, cur);

					ProgressBar folderProgressBar = (ProgressBar) rootView
							.findViewById(R.id.folderProgress);
					folderProgressBar.setMax(max);
					folderProgressBar.setProgress(cur);
				}
			}

			if (tape && fg && conn.service.isPlaying()) {
				tapeView.start();
			} else {
				tapeView.stop();
			}
		}
	}

	private void enDisViews() {
		if (tape) {
			tapePanel.setVisibility(View.VISIBLE);
			listPanel.setVisibility(View.GONE);
			progressText.setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.bottomButtons).setVisibility(View.GONE);
		} else {
			tapeView.stop();
			tapePanel.setVisibility(View.GONE);
			listPanel.setVisibility(View.VISIBLE);
			progressText.setVisibility(View.GONE);
			rootView.findViewById(R.id.bottomButtons).setVisibility(
					View.VISIBLE);
		}
		
		if (conn.service != null)
			conn.service.setVisible(tape);
	}

	private boolean fg;

	@Override
	public void onPause() {
		if (tapeView != null) {
			tapeView.stop();
		}
		fg = false;
		if (conn.service != null)
			conn.service.setVisible(false);
		handler.removeCallbacks(progressRunnable);
		super.onPause();
	}

	public void onResume() {
		super.onResume();
		progressRunnable.run();
		fg = true;
		if (conn.service != null)
			conn.service.setVisible(tape);
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
	}

}
