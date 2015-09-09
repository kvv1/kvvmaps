package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.files.LevelView.LevelProvider;
import kvv.aplayer.folders.Folder;
import kvv.aplayer.service.IAPService;
import android.view.View;
import android.view.View.OnClickListener;
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

		enDisViews();
	}

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
				progressText.setText(fold.files[file].getName());
				int dur = conn.service.getDuration();
				int pos = conn.service.getCurrentPosition();
				if (dur > 0) {
					int max = fold.files.length * 100;
					int cur = file * 100 + pos * 100 / dur;
					tapeView.setProgress(max, cur);
					
					ProgressBar folderProgressBar = (ProgressBar) rootView.findViewById(R.id.folderProgress);
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
			list.setVisibility(View.GONE);
			progressText.setVisibility(View.VISIBLE);

		} else {
			tapeView.stop();
			tapePanel.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
			progressText.setVisibility(View.GONE);
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

	protected void onNext() {
		if (conn.service != null
				&& conn.service.getFile() < conn.service.getFileCnt() - 1)
			seekStart(2000);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				seekEnd();
			}
		}, 500);
		super.onNext();
	}

	protected void onPrev() {
		super.onPrev();
		if (conn.service != null)
			seekStart(-2000);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				seekEnd();
			}
		}, 500);
	}

}
