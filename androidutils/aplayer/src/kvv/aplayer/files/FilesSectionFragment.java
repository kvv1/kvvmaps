package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.files.tape.LevelView;
import kvv.aplayer.files.tape.TapePanel;
import kvv.aplayer.files.tape.TapeView;
import kvv.aplayer.player.Files;
import kvv.aplayer.player.Player.PlayerAdapter;
import kvv.aplayer.player.Player.PlayerListener;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class FilesSectionFragment extends FilesSectionFragmentBase {

	private static boolean tape;

	private ListView listView;
	private TapePanel tapePanel;
	private View listPanel;
	private TapeView tapeView;
	private LevelView arrowLevelView;
	private LevelView magicEyeLevelView;

	private Handler handler = new Handler();

	private Runnable buttonsRunnable = new Runnable1() {
		@Override
		public void run1() {
			clearButtons();
		}
	};

	private PlayerListener listener = new PlayerAdapter() {
		@Override
		public void levelChanged(float indicatorLevel) {
			if (arrowLevelView != null && tape)
				arrowLevelView.setLevel(indicatorLevel);
			if (magicEyeLevelView != null && tape)
				magicEyeLevelView.setLevel(indicatorLevel);
		}

		public void folderChanged() {
			FilesAdapter adapter = new FilesAdapter(getActivity(), conn.service);
			listView.setAdapter(adapter);
		}

		public void fileChanged() {
			clearButtons();
			Files files = conn.service.getFiles();
			listView.invalidateViews();
			listView.setSelection(files.curFile - 2);
			updateTapeViewState();
		}
	};

	public FilesSectionFragment() {
		super(APService.class, R.layout.fragment_files);
	}

	private void restartButtonsTimer() {
		rootView.findViewById(R.id.goto1).setVisibility(View.VISIBLE);
		handler.removeCallbacks(buttonsRunnable);
		handler.postDelayed(buttonsRunnable, APActivity.BUTTONS_DELAY);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void createUI(final IAPService service) {
		super.createUI(service);

		rootView.findViewById(R.id.goto1).setVisibility(View.GONE);

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
					Files files = conn.service.getFiles();
					if (files.curFile < files.files.size() - 1)
						tapeView.setSeek(2000, true);
					conn.service.next();
				} else {
					if (conn.service.isPlaying())
						conn.service.pause();
					else
						conn.service.play();
				}
			}

			@Override
			protected void onLongClick(float touchX, float touchY) {
				int ht = tapeView.hitTest(touchX, touchY);
				if (ht == 0)
					showUndoRedoPanel();
			}

			@Override
			protected void onReleased(float touchX, float touchY) {
				if (tapeView != null)
					tapeView.setSeek(0, false);
			}
		};

		setCurrentSkin();

		service.addListener(listener);
		listener.folderChanged();
		listener.fileChanged();

		setMagicEye();
	}

	@Override
	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
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

	protected void setFolderProgress(int max, int cur) {
		super.setFolderProgress(max, cur);
		if (tapeView == null)
			return;
		if (tape)
			tapeView.setProgress(max, cur);
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
		updateTapeViewState();
		handler.removeCallbacksAndMessages(null);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateTapeViewState();
		setMagicEye();
	}

	public void setMagicEye() {
		handler.postDelayed(new Runnable1() {
			@Override
			public void run1() {
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

	@Override
	protected boolean isLevelNeeded() {
		return tape;
	}

	@Override
	protected void folderProgressClicked() {
		tape = !tape;
		setCurrentSkin();
	}

}
