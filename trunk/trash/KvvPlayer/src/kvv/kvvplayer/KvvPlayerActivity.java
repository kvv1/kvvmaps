package kvv.kvvplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import kvv.kvvplayer.R.id;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class KvvPlayerActivity extends Activity {

	private final static String root = "/sdcard/external_sd/books/";

	private MediaPlayer mp = new MediaPlayer();

	static class Folder {
		public Folder(String fol) {
			this.name = fol;
		}

		String name;
		ArrayList<String> files = new ArrayList<String>();
	}

	private ArrayList<Folder> folders = new ArrayList<Folder>();
	private int currentFolder;
	private int currentTrack;

	private TextView label;
	private ListView folders1;
	private ProgressBar progress;
	private ProgressBar trackProgress;
	private TelephonyManager telephonyManager;

	private Handler handler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					if (mp.isPlaying())
						mp.pause();
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);

		setContentView(R.layout.main);

		label = (TextView) findViewById(R.id.label);
		folders1 = (ListView) findViewById(R.id.folders);
		progress = (ProgressBar) findViewById(R.id.ProgressBar);
		trackProgress = (ProgressBar) findViewById(R.id.TrackProgress);

		addFiles(new File(root));
		Collections.sort(folders, new Comparator<Folder>() {
			@Override
			public int compare(Folder f1, Folder f2) {
				return f1.name.compareTo(f2.name);
			}
		});

		for (Folder f : folders)
			Collections.sort(f.files);

		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (Folder f : folders) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("folder", f.name);
			map.put("icon", R.drawable.inactive);
			list.add(map);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.listitem, new String[] { "folder", "icon" },
				new int[] { R.id.folder, R.id.icon });

		folders1.setAdapter(adapter);

		// folders1.setFocusable(false);
		// folders1.setFocusableInTouchMode(false);
		// folders1.setSelected(true);

		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				next(true);
			}
		});

		final Runnable seekForwardRunnable = new Runnable() {
			@Override
			public void run() {
				seekForward();
				handler.postDelayed(this, 200);
			}
		};

		final Runnable seekBackRunnable = new Runnable() {
			@Override
			public void run() {
				seekBack();
				handler.postDelayed(this, 200);
			}
		};

		Button seekForward = (Button) findViewById(id.seekforwark);
		// seekForward.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// // TODO Auto-generated method stub
		//
		// }
		// })
		seekForward.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handler.postDelayed(seekForwardRunnable, 200);
				return true;
			}
		});

		seekForward.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					handler.removeCallbacks(seekForwardRunnable);
					handler.removeCallbacks(seekBackRunnable);
				}
				return false;
			}
		});

		Button seekBack = (Button) findViewById(id.seekback);
		seekBack.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				handler.postDelayed(seekBackRunnable, 200);
				return true;
			}
		});

		seekBack.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					handler.removeCallbacks(seekForwardRunnable);
					handler.removeCallbacks(seekBackRunnable);
				}
				return false;
			}
		});

		Button prevDir = (Button) findViewById(id.prevdir);
		prevDir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prevFolder();
			}
		});

		Button nextDir = (Button) findViewById(id.nextdir);
		nextDir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextFolder();
			}
		});

		Button prev = (Button) findViewById(id.prev);
		prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prev();
			}
		});

		Button next = (Button) findViewById(id.next);
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				next(false);
			}
		});

		Button pause = (Button) findViewById(id.pause);
		pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mp.isPlaying())
					mp.pause();
				else
					mp.start();
			}
		});

		try {
			setDataSource();
			mp.prepare();
			mp.start();
			setLabels();
		} catch (Exception e) {
		}

	}

	private void setDataSource() throws Exception {
		String path = root + folders.get(currentFolder).name + "/"
				+ folders.get(currentFolder).files.get(currentTrack);
		mp.setDataSource(path);
	}

	private void setProgress() {
		progress.setProgress(mp.getCurrentPosition() * 100 / mp.getDuration());
		trackProgress.setProgress(currentTrack * 100
				/ folders.get(currentFolder).files.size());
	}

	private Runnable timerHandler = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(timerHandler);
			handler.postDelayed(timerHandler, 1000);
			setProgress();
		}
	};

	@Override
	protected void onPause() {
		handler.removeCallbacks(timerHandler);
		super.onPause();
	}

	@Override
	protected void onResume() {
		handler.removeCallbacks(timerHandler);
		handler.postDelayed(timerHandler, 100);
		super.onResume();
	}

	private String getFol(String path) {
		String fol = path.substring(KvvPlayerActivity.root.length());
		return fol.substring(0, fol.lastIndexOf('/'));
	}

	private void addFiles(File root) {
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				addFiles(file);
			} else if (file.getName().endsWith(".mp3")) {
				String fol = getFol(file.getAbsolutePath());
				Folder folder = null;
				for (Folder f : folders)
					if (f.name.equals(fol))
						folder = f;
				if (folder == null) {
					folder = new Folder(fol);
					folders.add(folder);
				}
				folder.files.add(file.getName());
			}
		}
	}

	int STEP = 10000;

	private void seekBack() {
		if (mp.isPlaying()) {
			int cur = mp.getCurrentPosition();

			if (cur < STEP && (currentFolder != 0 || currentTrack != 0)) {
				try {
					boolean playing = mp.isPlaying();
					mp.stop();
					mp.reset();
					if (currentTrack > 0)
						currentTrack--;
					else {
						currentFolder--;
						currentTrack = folders.get(currentFolder).files.size() - 1;
					}
					setDataSource();
					mp.prepare();
					if (playing)
						mp.start();
					setLabels();
					cur = mp.getDuration() - (STEP - cur);
				} catch (Exception e) {
				}
			}
			mp.seekTo(Math.max(0, cur - STEP));
		}
		setProgress();
	}

	private void seekForward() {
		if (mp.isPlaying()) {
			int dur = mp.getDuration();
			int cur = mp.getCurrentPosition();
			if (cur + STEP < dur)
				mp.seekTo(cur + STEP);
		}
		setProgress();
	}

	private int prevSel = -1;

	@SuppressWarnings("unchecked")
	private void setLabels() {
		Folder folder = folders.get(currentFolder);
		label.setText("" + (currentTrack + 1) + "(" + folder.files.size()
				+ ") " + folder.files.get(currentTrack));
		folders1.setSelection(currentFolder);

		if (prevSel >= 0) {
			HashMap<String, Object> sel = (HashMap<String, Object>) folders1
					.getItemAtPosition(prevSel);
			sel.put("icon", R.drawable.inactive);
		}

		prevSel = currentFolder;

		HashMap<String, Object> sel = (HashMap<String, Object>) folders1
				.getItemAtPosition(currentFolder);
		sel.put("icon", R.drawable.active);

		folders1.invalidateViews();
	}

	private void prev() {
		try {
			int cur = mp.getCurrentPosition();
			boolean playing = mp.isPlaying();
			mp.stop();
			mp.reset();
			if (cur < 1000 && (currentFolder > 0 || currentTrack > 0))
				if (currentTrack > 0)
					currentTrack--;
				else {
					currentFolder--;
					currentTrack = folders.get(currentFolder).files.size() - 1;
				}
			setDataSource();
			mp.prepare();
			if (playing)
				mp.start();
			setLabels();
			setProgress();
		} catch (Exception e) {
		}
	}

	private void next(boolean forcePlay) {
		try {
			if (currentFolder < folders.size() - 1
					|| currentTrack < folders.get(currentFolder).files.size() - 1) {
				boolean playing = mp.isPlaying();
				mp.stop();
				mp.reset();
				if (currentTrack < folders.get(currentFolder).files.size() - 1)
					currentTrack++;
				else {
					currentFolder++;
					currentTrack = 0;
				}
				setDataSource();
				mp.prepare();
				if (playing || forcePlay)
					mp.start();
				setLabels();
				setProgress();
			}
		} catch (Exception e) {
		}
	}

	private void nextFolder() {
		try {
			if (currentFolder < folders.size() - 1) {
				currentFolder++;
				currentTrack = 0;
				boolean playing = mp.isPlaying();
				mp.stop();
				mp.reset();
				setDataSource();
				mp.prepare();
				if (playing)
					mp.start();
				setLabels();
				setProgress();
			}
		} catch (Exception e) {
		}
	}

	private void prevFolder() {
		try {
			if (currentFolder > 0) {
				currentFolder--;
				currentTrack = 0;
				boolean playing = mp.isPlaying();
				mp.stop();
				mp.reset();
				setDataSource();
				mp.prepare();
				if (playing)
					mp.start();
				setLabels();
				setProgress();
			}
		} catch (Exception e) {
		}
	}

	private int cnt;

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (cnt == 0)
				prev();
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (cnt == 0)
				next(false);
		} else {
			return false;
		}
		cnt = 0;
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println("down " + event.getRepeatCount());
		cnt = event.getRepeatCount();
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (event.getRepeatCount() > 0) {
				seekBack();
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (event.getRepeatCount() > 0) {
				seekForward();
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			prevFolder();
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			nextFolder();
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (mp.isPlaying())
				mp.pause();
			else
				mp.start();
		} else {
			return false;
		}
		return true;
	}

}