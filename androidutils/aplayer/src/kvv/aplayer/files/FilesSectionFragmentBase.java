package kvv.aplayer.files;

import java.util.Random;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.player.Files;
import kvv.aplayer.player.Shuffle;
import kvv.aplayer.player.Player.OnChangedHint;
import kvv.aplayer.service.APServiceListener;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.Folder;
import kvv.aplayer.service.IAPService;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartbean.androidutils.fragment.FragmentX;
import com.smartbean.androidutils.util.Utils;

public abstract class FilesSectionFragmentBase extends
		FragmentX<APActivity, IAPService> implements APServiceListener {

	protected SharedPreferences settings;
	private Button pause;
	private TextView progressText;
	private ProgressBar fileProgressBar;
	protected TextView folderTextView;
	private ProgressBar folderProgressBar;

	private Handler handler = new Handler();

	private Runnable progressRunnable = new Runnable1() {
		@Override
		public void run1() {
			positionChanged();
			handler.removeCallbacks(this);
			handler.postDelayed(this, 1000);
		}
	};

	public FilesSectionFragmentBase(Class<?> serviceClass, int layout) {
		super(serviceClass, layout);
	}

	@Override
	public final void onChanged(OnChangedHint hint) {
		if (conn.service == null)
			return;
		switch (hint) {
		case FOLDER:
			folderChanged();
			break;
		case FILE:
			trackChanged();
		case STATE:
			stateChanged();
			setProgressColor();
		default:
			break;
		}
	}

	@Override
	public void onLevelChanged(float level) {
	}

	@Override
	public void onLoaded() {
	}

	private void setProgressColor() {
		Random rnd = Shuffle.getTodayRandom(1);
		// Random rnd = new Random();
		int color = Color.HSVToColor(new float[] { rnd.nextInt(360), 0.5f, 1 });

		folderProgressBar.getProgressDrawable().setColorFilter(color,
				PorterDuff.Mode.MULTIPLY);

		fileProgressBar.getProgressDrawable().setColorFilter(color,
				PorterDuff.Mode.MULTIPLY);
	}

	@Override
	protected void createUI(IAPService service) {
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		folderTextView = (TextView) rootView.findViewById(R.id.folder);
		fileProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
		folderProgressBar = (ProgressBar) rootView
				.findViewById(R.id.folderProgress);
		progressText = (TextView) rootView.findViewById(R.id.progressText);

		// folderProgressBar.getIndeterminateDrawable().setColorFilter(Color.RED,
		// PorterDuff.Mode.SRC_IN);
		// folderProgressBar.getProgressDrawable().setColorFilter(Color.RED,
		// PorterDuff.Mode.MULTIPLY);
		// fileProgressBar.getProgressDrawable().setColorFilter(Color.RED,
		// PorterDuff.Mode.MULTIPLY);

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
				showUndoRedoPanel();
				return true;
			}
		});

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

		Button prev = (Button) rootView.findViewById(R.id.prev);

		new TouchListener(prev) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service != null)
					conn.service.prev();
			}
		};

		Button next = (Button) rootView.findViewById(R.id.next);

		new TouchListener(next) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service != null)
					conn.service.next();
			}
		};

		service.addListener(this);
	}

	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(this);
		super.onDestroy();
	}

	protected void showUndoRedoPanel() {
		if (conn.service == null)
			return;

		MRUDialog mruDialog = new MRUDialog(getActivity(), conn.service);
		mruDialog.show();
	}

	private long[] folderFilesStartPos;
	private long folderMax;

	protected void folderChanged() {
		System.out.println("folderChanged()");
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
			// System.out.println("---  " + file.duration);
		}

	}

	protected void trackChanged() {
		System.out.println("trackChanged()");

		Files files = conn.service.getFiles();
		FileDescriptor file = files.getFile();
		if (file != null)
			progressText.setText(file.name);
	}

	protected void stateChanged() {
		positionChanged();
	}

	private void positionChanged() {
		if (conn.service != null) {
			int dur = conn.service.getDuration();
			int pos = conn.service.getCurrentPosition();

			fileProgressBar.setMax(dur);
			fileProgressBar.setProgress(pos);

			String time = Utils.convertSecondsToHMmSs(pos / 1000) + "("
					+ Utils.convertSecondsToHMmSs(dur / 1000) + ")";
			pause.setText((conn.service.isPlaying() ? "Pause" : "Play") + "  "
					+ time);

			Files files = conn.service.getFiles();
			if (files.files.size() > 0 && folderFilesStartPos != null
					&& folderFilesStartPos.length > files.curFile) {
				int max = (int) (folderMax / 1000);
				int cur = (int) (folderFilesStartPos[files.curFile] + pos) / 1000;

				setFolderProgress(max, cur);
			}
		}
	}

	protected void setFolderProgress(int max, int cur) {
		folderProgressBar.setMax(max);
		folderProgressBar.setProgress(cur);
	}

	@Override
	public void onPause() {
		if (conn.service != null)
			conn.service.setVisible(false);
		handler.removeCallbacksAndMessages(null);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (conn.service != null)
			conn.service.setVisible(isLevelNeeded());
		progressRunnable.run();
	}

	protected abstract boolean isLevelNeeded();
}
