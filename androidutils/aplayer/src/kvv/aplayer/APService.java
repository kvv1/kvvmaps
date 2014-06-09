package kvv.aplayer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.smartbean.androidutils.service.BaseService;

public class APService extends BaseService {
	public final static File ROOT = new File(
			Environment.getExternalStorageDirectory(), "/external_sd/aplayer/");

	private Set<APServiceListener> listeners = new HashSet<APServiceListener>();

	private Player player;

	private Handler handler = new Handler();

	class Saver implements Runnable {
		@Override
		public void run() {
			save();
			handler.postDelayed(this, 10000);
		}
	}

	private Saver saver;

	private SharedPreferences settings;

	private TelephonyManager telephonyManager;
	private PhoneStateListener phoneStateListener;

	public List<Bookmark> bookmarks;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			player.setMaxVolume();

			String action = intent.getAction();
			System.out.println("BroadcastReceiver " + action);

			if ("kvv.aplayer.PAUSE".equals(action)) {
				player.pause();
			}
			if ("kvv.aplayer.PLAY_PAUSE".equals(action)) {
				player.play_pause();
			}
			if ("kvv.aplayer.PREV".equals(action)) {
				player.prev();
			}
			if ("kvv.aplayer.NEXT".equals(action)) {
				player.next(true);
			}

			if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				player.pause();
			}
		}
	};

	public APService() {
		super(R.drawable.ic_launcher, R.drawable.ic_launcher, APActivity.class,
				R.string.app_name, R.string.app_name, false, false);
	}

	@Override
	public void onCreate() {
		List<Folder> folders = new ArrayList<Folder>();
		readFolders(ROOT, 0, folders);

		player = new Player(folders) {
			@Override
			protected void onChanged() {
				System.out.println("onChanged " + isPlaying());

				if (!isPlaying()) {
					handler.removeCallbacks(saver);
					saver = null;
				} else {
					if (saver == null) {
						saver = new Saver();
						handler.post(saver);
					}
				}

				for (APServiceListener l : listeners)
					l.onChanged();
			}

			@Override
			protected void onRandomChanged() {
				for (APServiceListener l : listeners)
					l.onRandomChanged();
			}
		};
		super.onCreate();
		settings = PreferenceManager.getDefaultSharedPreferences(this);

		String sBookmarks = settings.getString("Bookmarks", null);
		if (sBookmarks != null)
			bookmarks = new ArrayList<Bookmark>(Arrays.asList(new Gson()
					.fromJson(sBookmarks, Bookmark[].class)));
		else
			bookmarks = new ArrayList<Bookmark>();

		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					if (player.isPlaying())
						player.play_pause();
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};

		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		IntentFilter filter = new IntentFilter();
		filter.addAction("kvv.aplayer.PAUSE");
		filter.addAction("kvv.aplayer.PLAY_PAUSE");
		filter.addAction("kvv.aplayer.NEXT");
		filter.addAction("kvv.aplayer.PREV");
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(broadcastReceiver, filter);

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
				RemoteControlReceiver.class.getName()));
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		player.close();
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		super.onDestroy();
	}

	private void saveBookmarks() {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("Bookmarks",
				new Gson().toJson(bookmarks.toArray(new Bookmark[0])));
		editor.commit();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new APServiceBinder() {
		};
	}

	public class APServiceBinder extends Binder implements IAPService {
		@Override
		public List<Folder> getFolders() {
			return player.getFolders();
		}

		@Override
		public List<Bookmark> getBookmarks() {
			return bookmarks;
		}

		@Override
		public void addBookmark() {
			Folder folder = getFolders().get(getCurrentFolder());
			bookmarks.add(new Bookmark(folder.displayName,
					folder.files[getFile()].getName(), getDuration(),
					getCurrentPosition()));
			for (APServiceListener l : listeners)
				l.onBookmarksChanged();
			saveBookmarks();
		}

		@Override
		public void delBookmark(Bookmark bookmark) {
			bookmarks.remove(bookmark);
			for (APServiceListener l : listeners)
				l.onBookmarksChanged();
			saveBookmarks();
		}

		@Override
		public void addListener(APServiceListener listener) {
			listeners.add(listener);
		}

		@Override
		public void removeListener(APServiceListener listener) {
			listeners.remove(listener);
		}

		@Override
		public int getCurrentFolder() {
			return player.getCurrentFolder();
		}

		@Override
		public void toFolder(int position) {
			if (position == player.getCurrentFolder())
				return;

			save();

			Folder folder = player.getFolders().get(position);
			int curFile = settings.getInt(folder.path + "|file", 0);
			int curPos = settings.getInt(folder.path + "|pos", 0);
			if (curFile >= folder.files.length)
				curFile = 0;

			player.toFolder(position, curFile, curPos);
		}

		@Override
		public void toRandom(int position) {
			save();
			player.makeRandom(position);
		}

		@Override
		public void toFile(int position) {
			player.toFile(position);
		}

		@Override
		public void prev() {
			player.prev();
		}

		@Override
		public void next() {
			player.next(true);
		}

		@Override
		public void play_pause() {
			player.play_pause();
		}

		@Override
		public int getDuration() {
			return player.getDuration();
		}

		@Override
		public int getCurrentPosition() {
			return player.getCurrentPosition();
		}

		@Override
		public void seek(int seekStep) {
			player.seek(seekStep);
		}

		@Override
		public int getFile() {
			return player.getFile();
		}

		@Override
		public boolean isPlaying() {
			return player.isPlaying();
		}

		@Override
		public void toBookmark(Bookmark bookmark) {
			int f = 0;
			for (Folder folder : getFolders()) {
				if (folder.displayName.equals(bookmark.folder)) {
					int t = 0;
					for (File file : folder.files) {
						if (file.getName().equals(bookmark.track)) {
							if (f != player.getCurrentFolder())
								save();
							player.toFolder(f, t, bookmark.time);
						}
						t++;
					}
				}
				f++;
			}
		}

	}

	private void readFolders(File dir, int indent, List<Folder> folders) {

		File[] dirs = listDirs(dir);
		for (File d : dirs) {

			Folder folder = new Folder(d.getAbsolutePath(), indent,
					listFiles(d));

			folders.add(folder);
			readFolders(d, indent + 1, folders);
		}
	}

	private static File[] listFiles(File dir) {
		File[] res = dir.listFiles(new FileFilter() {
			@SuppressLint("DefaultLocale")
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile()
						&& pathname.getName().toLowerCase().endsWith(".mp3");
			}
		});
		Arrays.sort(res, new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		return res;
	}

	private static File[] listDirs(File dir) {
		File[] res = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		Arrays.sort(res, new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		return res;
	}

	private void save() {
		if (player.getCurrentFolder() < 0)
			return;

		Folder folder = player.getFolders().get(player.getCurrentFolder());

		System.out.println("SAVE " + folder.shortName + " " + player.getFile());

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(folder.path + "|file", player.getFile());
		editor.putInt(folder.path + "|pos", player.getCurrentPosition());
		editor.commit();
	}

}