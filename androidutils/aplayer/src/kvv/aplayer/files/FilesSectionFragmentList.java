package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.files.LevelView.LevelProvider;
import kvv.aplayer.service.File1;
import kvv.aplayer.service.IAPService;
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

public class FilesSectionFragmentList extends FilesSectionFragment {

	private static boolean tape;

	private ListView list;
	private View tapePanel;
	private View listPanel;
	private TapeView tapeView;
	private LevelView levelView;
	private TextView progressText;
	private Button pause;

	private Runnable buttonsRunnable = new Runnable() {
		@Override
		public void run() {
			if (rootView != null) {
				clearButtons();
			}
		}
	};

	@Override
	protected void restartButtonsTimer() {
		handler.removeCallbacks(buttonsRunnable);
		handler.postDelayed(buttonsRunnable, APActivity.BUTTONS_DELAY);
	}

	@Override
	protected void createUI(final IAPService service) {
		super.createUI(service);
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
		levelView.setLevelProvider(new LevelProvider() {
			@Override
			public float getLevel() {
				if (conn.service == null)
					return 0;
				return conn.service.getLevel();
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
				int ht = tapeView.hitTest(touchX, touchY);
				switch (ht) {
				case -1:
					prevClick();
					break;
				case 1:
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
				if (ht == 1)
					nextLongClick();
				else if (ht == -1)
					prevLongClick();
				else
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
				int height = rootView.getMeasuredHeight();

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
	}

	public void onDestroy() {
		super.onDestroy();
	}

	float touchX;
	float touchY;

	private void clearButtons() {
		handler.removeCallbacks(buttonsRunnable);
		// if (!settings.getBoolean(getString(R.string.prefTestMode), false))
		rootView.findViewById(R.id.extButtons).setVisibility(View.GONE);
		rootView.findViewById(R.id.buttons).setVisibility(View.GONE);
		FilesAdapter adapter = (FilesAdapter) list.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			list.invalidateViews();
		}
	}

	@Override
	protected void folderChanged() {
		FilesAdapter adapter = new FilesAdapter(getActivity(), conn.service);
		list.setAdapter(adapter);
	}

	@Override
	protected void trackChanged() {
		clearButtons();
		list.invalidateViews();
		list.setSelection(conn.service.getFile() - 2);
	}

	protected void updateUI() {
		super.updateUI();

		if (conn.service != null) {
			pause.setText(conn.service.isPlaying() ? "Pause" : "Play");

			int file = conn.service.getFile();
			File1[] files = conn.service.getFiles();
			if (files.length > 0) {
				progressText.setText(files[file].name);
				int dur = conn.service.getDuration();
				int pos = conn.service.getCurrentPosition();
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

					// int max = files.length * 100;
					// int cur = file * 100 + pos * 100 / dur;
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

			if (tape && fg)
				levelView.start();

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
	}

	private boolean fg;

	@Override
	public void onPause() {
		if (tapeView != null) {
			tapeView.stop();
			levelView.stop();
		}
		fg = false;
		super.onPause();
	}

	public void onResume() {
		super.onResume();
		fg = true;
	}

	@Override
	protected void seekStart(int step) {
		if (tapeView != null && tape)
			tapeView.setSeek(step);
	}

	@Override
	protected void seekEnd() {
		if (tapeView != null && tape)
			tapeView.setSeek(0);
	}

	private Runnable seekRunnable = new Runnable() {
		@Override
		public void run() {
			seekEnd();
		}
	};

	protected void onNext() {
		if (conn.service != null
				&& conn.service.getFile() < conn.service.getFileCnt() - 1)
			seekStart(2000);
		handler.removeCallbacks(seekRunnable);
		handler.postDelayed(seekRunnable, 500);
		super.onNext();
	}

	protected void onPrev() {
		super.onPrev();
		if (conn.service != null)
			seekStart(-2000);
		handler.removeCallbacks(seekRunnable);
		handler.postDelayed(seekRunnable, 500);
	}

	OnClickListener prevOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			prevClick();
		}
	};

	OnLongClickListener prevOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			prevLongClick();
			return true;
		}
	};

	OnTouchListener prevOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			touch(event);
			return false;
		}
	};

	OnClickListener nextOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			nextClick();
		}
	};

	OnLongClickListener nextOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			nextLongClick();
			return true;
		}
	};

	OnTouchListener nextOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			touch(event);
			return false;
		}
	};
}
