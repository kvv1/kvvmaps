package kvv.aplayer.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.player.Files;
import kvv.aplayer.player.Folders;
import kvv.aplayer.player.Player.PlayerAdapter;
import kvv.aplayer.player.Player.PlayerListener;
import kvv.aplayer.player.PlayerUndoRedo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.smartbean.androidutils.service.BaseService;

public class APService extends BaseService implements IAPService {
	public static APService staticInstance;

	private PlayerUndoRedo player;

	private Handler handler = new Handler();

	private SharedPreferences settings;

	private TelephonyManager telephonyManager;
	private PhoneStateListener phoneStateListener;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			System.out.println("BroadcastReceiver " + action);

			if ("kvv.aplayer.PAUSE".equals(action)) {
				player.pause();
			}
			if ("kvv.aplayer.PLAY_PAUSE".equals(action)) {
				if (player.isPlaying())
					player.pause();
				else
					player.play();
			}
			if ("kvv.aplayer.PREV".equals(action)) {
				player.prev();
			}
			if ("kvv.aplayer.NEXT".equals(action)) {
				player.next();
			}

			if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				if (isCarMode())
					player.pause();
			}
		}
	};

	public APService() {
		super(R.drawable.ap, R.drawable.ap, APActivity.class,
				R.string.app_name, R.string.app_name, false, false);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		settings = PreferenceManager.getDefaultSharedPreferences(this);

		createPlayer();

		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING)
					player.pause();
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

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		staticInstance = this;

		player.init();
	}

	private void createPlayer() {
		if (player != null)
			player.close();

		List<Folder> folders = read();
		player = new PlayerUndoRedo(this, folders);

		player.addListener(new PlayerAdapter() {
			@Override
			public void fileChanged() {
				if (isCarMode())
					setMaxVolume();

				if (!isPlaying())
					stopGpsDelayed();
				else
					startGps();
			}
		});
		
		modeChanged();

		for (PlayerListener l : player.listeners)
			l.folderListChanged();
	}

	@Override
	public void onDestroy() {
		staticInstance = null;
		unregisterReceiver(broadcastReceiver);
		player.close();
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		stopGpsRunnable.run();
		super.onDestroy();
	}

	@Override
	public void addBadSong(String path) {
		player.addBadSong(path);
	}

	@Override
	public void delBadSong(String path) {
		player.delBadSong(path);
	}

	@Override
	public List<String> getBadSongs() {
		return player.getBadSongs();
	}

	@Override
	public Folders getFolders() {
		return player.getFolders();
	}

	@Override
	public void addListener(PlayerListener listener) {
		player.addListener(listener);
	}

	@Override
	public void removeListener(PlayerListener listener) {
		player.removeListener(listener);
	}

	@Override
	public void toFolder(int folderIdx) {
		Folders folders = player.getFolders();
		if (folderIdx == folders.curFolder)
			return;
		player.toFolder(folderIdx);
	}

	@Override
	public List<String> getMRU() {
		return player.getMRU();
	}

	@Override
	public void setRandom() {
		player.setRandom();
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
		if (player.hasNext()) {
			player.next();
		}
	}

	@Override
	public void play_pause() {
		if (player.isPlaying())
			player.pause();
		else
			player.play();
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
	public void seekTo(int f) {
		player.seekTo(f);
	}

	@Override
	public boolean isPlaying() {
		return player.isPlaying();
	}

	@Override
	public void modeChanged() {
		player.setCompr(isCarMode() ? 15 : 0);
		player.setDbPer100(isCarMode() ? 5 : 0);
	}

	@Override
	public void reload() {
		createPlayer();
	}

	@Override
	public Files getFiles() {
		return player.getFiles();
	}

	@Override
	public void setVisible(boolean vis) {
		player.setVisible(vis);
	}

	@Override
	public void undo() {
		System.out.println("undo");
		player.undo();
	}

	@Override
	public void redo() {
		System.out.println("redo");
		player.redo();
	}

	private LocationManager locationManager;
	private LocationListener locationListener;

	private Runnable stopGpsRunnable = new Runnable() {
		@Override
		public void run() {
			if (locationListener != null) {
				locationManager.removeUpdates(locationListener);
				locationListener = null;
			}
		}
	};

	private boolean isCarMode() {
		return settings.getBoolean(getString(R.string.prefCarMode), false);
	}

	private void startGps() {
		if (!isCarMode())
			return;

		handler.removeCallbacks(stopGpsRunnable);
		if (locationListener == null) {
			locationListener = new MyLocationListener() {
				float speed;

				@Override
				public void onLocationChanged(Location location) {
					if (!location.hasSpeed())
						return;

					float s = location.getSpeed() * 3.6f;
					if (speed > 30 && s < 5)
						return;
					speed = s;
					player.setSpeedKMH(speed);
				}
			};
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		}
	}

	private void stopGpsDelayed() {
		handler.removeCallbacks(stopGpsRunnable);
		handler.postDelayed(stopGpsRunnable, 600000);
	}

	static abstract class MyLocationListener implements LocationListener {

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}

	}

	private List<Folder> read() {

		List<Folder> folders = new ArrayList<Folder>();

		Cursor mCursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.DATA,
						MediaStore.Audio.Media.DURATION }, null, null, null);

		if (mCursor == null)
			return folders;

		Map<String, List<FileDescriptor>> map = new HashMap<String, List<FileDescriptor>>();

		while (mCursor.moveToNext()) {
			String title = mCursor
					.getString(mCursor
							.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

			String path = mCursor.getString(mCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

			long dur = mCursor.getLong(mCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
			// System.out.println(title + " " + path + " " + dur);

			if (path.contains("/_/")
					&& !settings
							.getBoolean(getString(R.string.prefTest), false))
				continue;

			String p = path.substring(path.indexOf('/', 1) + 1);
			for (int i = 0; i < p.length(); i++)
				if (p.charAt(i) == '/') {
					String fold = p.substring(0, i);
					if (!map.containsKey(fold))
						map.put(fold, new ArrayList<FileDescriptor>());
				}

			String folder = p.substring(0, p.lastIndexOf('/'));

			List<FileDescriptor> files = map.get(folder);
			files.add(new FileDescriptor(path, title, dur));
		}
		mCursor.close();
		System.out.println();

		List<String> folds = new ArrayList<String>(map.keySet());
		Collections.sort(folds);

		for (String folder : folds) {
			int ind = 0;
			for (int i = 0; i < folder.length(); i++)
				if (folder.charAt(i) == '/')
					ind++;

			List<FileDescriptor> files = map.get(folder);
			Collections.sort(files);

			folders.add(new Folder(folder, ind, files));
		}

		return folders;
	}

	private void setMaxVolume() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		System.out.println("maxVol " + maxVol);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
	}

}