package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.player.Files;
import kvv.aplayer.player.Player.PlayerAdapter;
import kvv.aplayer.player.Player.PlayerListener;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.Folder;
import kvv.aplayer.service.IAPService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartbean.androidutils.activity.FragmentActivityTabsNoActionBar.FragmentEx;
import com.smartbean.androidutils.fragment.FragmentX;
import com.smartbean.androidutils.util.Utils;

public abstract class FilesSectionFragmentBase extends FragmentX<APActivity, IAPService> implements FragmentEx {

	protected SharedPreferences settings;
	private Button pause;
	private TextView progressText;
	private ProgressBar fileProgressBar;
	private TextView folderTextView;
	private ProgressBar folderProgressBar;
	private TextView trackTimeView;
	private TextView folderTimeView;

	private Handler handler = new Handler();

	protected boolean resumed;
	protected boolean visible;

	private Runnable progressRunnable = new Runnable1() {
		@Override
		public void run1() {
			onProgress();
			handler.removeCallbacks(this);
			handler.postDelayed(this, 1000);
		}
	};

	protected void onProgress() {
		setFolderProgress();
		setFileProgress();
	}

	public FilesSectionFragmentBase(Class<?> serviceClass, int layout) {
		super(serviceClass, layout);
	}

	private PlayerListener listener = new PlayerAdapter() {
		@Override
		public void folderChanged() {
			// System.out.println("folderChanged()");
			Folder folder = conn.service.getFolders().getFolder();
			if (folder != null)
				folderTextView.setText(folder.getDisplayName());
			else
				folderTextView.setText("<empty>");

			Files files = conn.service.getFiles();

			folderMax = 0;
			folderFilesStartPos = new long[files.files.size()];
			for (int i = 0; i < folderFilesStartPos.length; i++) {
				FileDescriptor file = files.files.get(i);
				folderFilesStartPos[i] = folderMax;
				folderMax += file.duration;
			}
		}

		public void fileChanged() {
			if (conn.service == null)
				return;

			// System.out.println("trackChanged()");
			Files files = conn.service.getFiles();
			FileDescriptor file = files.getFile();
			if (file != null)
				progressText.setText(file.name);
			trackTimeView.setText("");

			pause.setText(conn.service.isPlaying() ? "Pause" : "Play");

			setFileProgress();
			setFolderProgress();
		}
	};

	private void setFileProgress() {
		if (conn.service == null)
			return;
		int dur = conn.service.getDuration();
		int pos = conn.service.getCurrentPosition();
		fileProgressBar.setMax(dur);
		fileProgressBar.setProgress(pos);
		String time = Utils.convertSecondsToHMmSs(pos / 1000) + "(" + Utils.convertSecondsToHMmSs(dur / 1000) + ")";
		trackTimeView.setText(time);
	}

	@Override
	protected void createUI(IAPService service) {

		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		folderTextView = (TextView) rootView.findViewById(R.id.folder);
		fileProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
		folderProgressBar = (ProgressBar) rootView.findViewById(R.id.folderProgress);
		progressText = (TextView) rootView.findViewById(R.id.progressText);
		trackTimeView = (TextView) rootView.findViewById(R.id.tracktime);
		folderTimeView = (TextView) rootView.findViewById(R.id.foldertime);

		pause = (Button) rootView.findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rootView.getContext().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
						Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)));
			}
		});

		pause.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showUndoRedoPanel();
				return true;
			}
		});

		final View fileprogresstouch = rootView.findViewById(R.id.fileprogresstouch);
		new TouchListener(fileprogresstouch) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service != null) {
					int dur = conn.service.getDuration();
					int pos = (int) (dur * touchX / fileprogresstouch.getWidth());
					// System.out.println("seek to " + pos);
					conn.service.seekTo(pos);
				}
			}
		};

		Button prev = (Button) rootView.findViewById(R.id.prev);

		new TouchListener(prev) {
			@Override
			protected void onClick(float touchX, float touchY) {
				rootView.getContext().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
						Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)));
			}
		};

		Button next = (Button) rootView.findViewById(R.id.next);

		new TouchListener(next) {
			@Override
			protected void onClick(float touchX, float touchY) {
				rootView.getContext().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
						Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)));
			}
		};

		Button back10s = (Button) rootView.findViewById(R.id.back10s);

		new TouchListener(back10s) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service != null) {
					int pos = conn.service.getCurrentPosition();
					conn.service.seekTo(Math.max(0, pos - 10000));
				}
			}
		};

		service.addListener(listener);
		listener.folderChanged();
		listener.fileChanged();
	}

	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
		handler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	protected void showUndoRedoPanel() {
		if (conn.service == null)
			return;

		PopupDialog mruDialog = new PopupDialog(getActivity(), conn.service);
		mruDialog.show();
	}

	private long[] folderFilesStartPos;
	private long folderMax;

	protected void setFolderProgress(int max, int cur) {
		folderProgressBar.setMax(max);
		folderProgressBar.setProgress(cur);
		String time = Utils.convertSecondsToHMm(cur) + "(" + Utils.convertSecondsToHMm(max) + ")";
		folderTimeView.setText(time);
	}

	private void setFolderProgress() {
		if (conn.service == null)
			return;

		Files files = conn.service.getFiles();
		if (files.files.size() > 0 && folderFilesStartPos != null && folderFilesStartPos.length > files.curFile) {
			int pos = conn.service.getCurrentPosition();
			int max = (int) (folderMax / 1000);
			int cur = (int) (folderFilesStartPos[files.curFile] + pos) / 1000;
			setFolderProgress(max, cur);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		resumed = false;
		startStopAnim();
	}

	@Override
	public void onResume() {
		super.onResume();
		resumed = true;
		startStopAnim();
	}

	@Override
	public void onSelected(boolean b) {
		Utils.log(this, "onSelected " + b);
		visible = b;
		startStopAnim();
	}

	protected void startStopAnim() {
		if (resumed && visible) {
			progressRunnable.run();
		} else {
			handler.removeCallbacksAndMessages(null);
		}
	}
}
