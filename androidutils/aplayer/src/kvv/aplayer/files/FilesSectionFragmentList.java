package kvv.aplayer.files;

import java.io.File;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.files.LevelView.LevelProvider;
import kvv.aplayer.folders.Folder;
import kvv.aplayer.service.IAPService;
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

public class FilesSectionFragmentList extends FilesSectionFragment {

	private static boolean tape;

	private ListView list;
	private View tapePanel;
	private View listPanel;
	private TapeView tapeView;
	private LevelView levelView;
	TextView progressText;

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
				if (touchY > v.getWidth() / 3) {
					playPause();
				} else if (touchY > v.getWidth() / 15) {
					if (touchX < v.getWidth() / 2)
						prevClick();
					else
						nextClick();
				}
			}
		});

		tapeView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (touchY > v.getWidth() / 3) {
				} else if (touchY > v.getWidth() / 15) {
					if (touchX < v.getWidth() / 2)
						prevLongClick();
					else
						nextLongClick();
				}
				return false;
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

		enDisViews();
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
			int file = conn.service.getFile();
			int folder = conn.service.getCurrentFolder();
			if (folder >= 0) {
				Folder fold = conn.service.getFolders().get(folder);
				if (fold.files != null) {
					progressText.setText(new File(fold.files[file]).getName());
					int dur = conn.service.getDuration();
					int pos = conn.service.getCurrentPosition();
					if (dur > 0) {
						int max = fold.files.length * 100;
						int cur = file * 100 + pos * 100 / dur;
						tapeView.setProgress(max, cur);

						ProgressBar folderProgressBar = (ProgressBar) rootView
								.findViewById(R.id.folderProgress);
						folderProgressBar.setMax(max);
						folderProgressBar.setProgress(cur);
					}
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

}
