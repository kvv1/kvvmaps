package kvv.aplayer;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartbean.androidutils.fragment.RLFragment;

public class FilesSectionFragment extends RLFragment<APActivity, IAPService> {

	private boolean noSel;

	private Handler handler = new Handler();

	private Button pause;
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
				conn.service.seekForward(seekStep);
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
				conn.service.seekBack(seekStep);
				seekStep = seekStep + seekStep / 3;
				if (seekStep > 20000)
					seekStep = 20000;
			}
			updateUI();
			handler.postDelayed(this, 200);
		}
	};

	private void updateUI() {
		if (conn.service == null)
			return;
		progressBar.setMax(conn.service.getDuration());
		progressBar.setProgress(conn.service.getCurrentPosition());
		pause.setText(conn.service.isPlaying() ? "Pause" : "Play");
	}

	private final APServiceListener listener = new APServiceListener() {
		@Override
		public void onChanged() {
			if (conn.service == null)
				return;

			if (conn.service.getCurrentFolder() != folder
					&& conn.service.getFolders().size() > 0) {
				FilesAdapter adapter = new FilesAdapter(getActivity(),
						conn.service);
				list.setAdapter(adapter);
				folder = conn.service.getCurrentFolder();

				Folder fold = conn.service.getFolders().get(folder);
				TextView folderTextView = (TextView) rootView
						.findViewById(R.id.folder);
				folderTextView.setText(fold.displayName);

			}

			list.invalidateViews();
			if (!noSel)
				list.setSelection(conn.service.getFile());

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

	@Override
	protected void createUI(final IAPService service) {
		folder = -1;
		list = (ListView) rootView.findViewById(R.id.list);

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView,
					View view, int position, long id) {
				if (conn.service != null)
					try {
						noSel = true;
						conn.service.toFile(position);
					} finally {
						noSel = false;
					}
				return false;
			}

		});

		// list.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// service.pause();
		// }
		// });

		// list.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> adapterView, View view,
		// int position, long id) {
		// if (conn.service != null)
		// try {
		// noSel = true;
		// conn.service.toFile(position);
		// } finally {
		// noSel = false;
		// }
		// }
		// });

		Button prev = (Button) rootView.findViewById(R.id.prev);
		prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				service.prev();
			}
		});

		prev.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				seekStep = 1000;
				handler.postDelayed(seekBackRunnable, 200);
				return true;
			}
		});

		prev.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					handler.removeCallbacks(seekForwardRunnable);
					handler.removeCallbacks(seekBackRunnable);
				}
				return false;
			}
		});

		Button next = (Button) rootView.findViewById(R.id.next);
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				service.next();
			}
		});

		next.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				seekStep = 1000;
				handler.postDelayed(seekForwardRunnable, 200);
				return true;
			}
		});

		next.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					handler.removeCallbacks(seekForwardRunnable);
					handler.removeCallbacks(seekBackRunnable);
				}
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

		progressBar = (ProgressBar) rootView.findViewById(R.id.progress);

		service.addListener(listener);
		listener.onChanged();
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
