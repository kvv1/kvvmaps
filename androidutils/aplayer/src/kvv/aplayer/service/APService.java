package kvv.aplayer.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.RemoteControlReceiver;
import kvv.aplayer.player.IPlayer;
import kvv.aplayer.player.Player1;
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
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.smartbean.androidutils.service.BaseService;

public class APService extends BaseService implements IAPService {
	public final static File ROOT = new File(
			Environment.getExternalStorageDirectory(), "/external_sd/aplayer/");

	public static final float[] dBPer100kmh = { 0, 5f };
	public static final int[] compr = { 0, 15 };

	private Set<APServiceListener> listeners = new HashSet<APServiceListener>();

	private IPlayer player;

	private Handler handler = new Handler();

	class Saver implements Runnable {
		@Override
		public void run() {
			save();
			handler.postDelayed(this, 60000);
		}
	}

	private Saver saver;

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
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(APService.this);
				if (settings.getBoolean(getString(R.string.prefNavigatorMode),
						false))
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
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					if (player.isPlaying())
						player.pause();
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

		player.setGain(settings.getInt("gain", 0));

		int comprIdx = settings.getInt("comprIdx", 0);
		if (comprIdx >= compr.length) {
			comprIdx = 0;
			setPrefInt("comprIdx", comprIdx);
		}
		player.setCompr(compr[comprIdx]);

		int dBPer100Idx = settings.getInt("dBPer100Idx", 0);
		if (dBPer100Idx >= dBPer100kmh.length) {
			dBPer100Idx = 0;
			setPrefInt("dBPer100Idx", dBPer100Idx);
		}
		player.setDbPer100(dBPer100kmh[dBPer100Idx]);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	private void createPlayer() {
		if (player != null)
			player.close();

		undoList.clear();
		redoList.clear();

		List<Folder> folders = read();
		player = new Player1(folders) {
			@Override
			public void onChanged(OnChangedHint hint) {
				super.onChanged(hint);
				System.out.println("onChanged " + isPlaying());
				
				setMaxVolume();

				if (!isPlaying()) {
					handler.removeCallbacks(saver);
					saver = null;
					stopGpsDelayed();
				} else {
					if (saver == null) {
						saver = new Saver();
						handler.post(saver);
					}
					startGps();
				}

				for (APServiceListener l : listeners)
					l.onChanged(hint);
			}
		};
		for (APServiceListener l : listeners)
			l.onLoaded();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		player.close();
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		stopGpsRunnable.run();
		super.onDestroy();
	}

	// @Override
	// public IBinder onBind(Intent arg0) {
	// return new APServiceBinder() {
	// };
	// }
	//
	// public class APServiceBinder extends Binder implements IAPService {
	@Override
	public List<Folder> getFolders() {
		return player.getFolders();
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

		storeUndo();

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
		storeUndo();
		save();
		player.makeRandom(position);
	}

	@Override
	public void toFile(int position) {
		storeUndo();
		player.toFile(position);
	}

	@Override
	public void prev() {
		storeUndo();
		player.prev();
	}

	@Override
	public void next() {
		storeUndo();
		player.next();
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
	public void setGain(int db) {
		player.setGain(db);
		setPrefInt("gain", db);
	}

	@Override
	public void setComprIdx(int n) {
		player.setCompr(compr[n]);
		setPrefInt("comprIdx", n);
	}

	@Override
	public int getComprIdx() {
		return settings.getInt("comprIdx", 0);
	}

	@Override
	public void setDBPer100Idx(int n) {
		player.setDbPer100(dBPer100kmh[n]);
		setPrefInt("dBPer100Idx", n);
	}

	@Override
	public int getDBPer100Idx() {
		return settings.getInt("dBPer100Idx", 0);
	}

	@Override
	public int getGain() {
		return player.getGain();
	}

	@Override
	public float getLevel() {
		return player.getLevel();
	}

	@Override
	public int getFileCnt() {
		int folder = getCurrentFolder();
		if (folder >= 0) {
			Folder fold = getFolders().get(folder);
			return fold.files.length;
		}
		return 0;
	}

	@Override
	public void reload() {
		createPlayer();
	}

	@Override
	public File1[] getFiles() {
		int folder = getCurrentFolder();
		if (folder < 0)
			return new File1[0];
		return getFolders().get(folder).files;
	}

	@Override
	public void redo() {
		System.out.println("redo");
		if (redoList.isEmpty())
			return;
		// if(lastItem == null)
		UndoItem lastItem = new UndoItem(player.getCurrentFolder(),
				player.getFile(), player.getCurrentPosition());
		UndoItem item = redoList.removeLast();
		undoList.add(lastItem);
		// lastItem = item;
		player.toFolder(item.folder, item.file, item.pos);
	}

	@Override
	public void undo() {
		System.out.println("undo");
		if (undoList.isEmpty())
			return;
		// if(lastItem == null)
		UndoItem lastItem = new UndoItem(player.getCurrentFolder(),
				player.getFile(), player.getCurrentPosition());
		UndoItem item = undoList.removeLast();
		redoList.add(lastItem);
		// lastItem = item;
		player.toFolder(item.folder, item.file, item.pos);
	}

	@Override
	public void setVisible(boolean vis) {
		player.setVisible(vis);
	}

	// }

	private void storeUndo() {
		if (player.getCurrentFolder() < 0)
			return;
		undoList.add(new UndoItem(player.getCurrentFolder(), player.getFile(),
				player.getCurrentPosition()));
		redoList.clear();
	}

	static class UndoItem {
		int folder;
		int file;
		int pos;

		public UndoItem(int folder, int file, int pos) {
			super();
			this.folder = folder;
			this.file = file;
			this.pos = pos;
		}
	}

	private LinkedList<UndoItem> undoList = new LinkedList<APService.UndoItem>();
	private LinkedList<UndoItem> redoList = new LinkedList<APService.UndoItem>();

	private void save() {
		if (player.getCurrentFolder() < 0)
			return;

		Folder folder = player.getFolders().get(player.getCurrentFolder());

		// System.out.println("SAVE " + folder.shortName + " " +
		// player.getFile());

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(folder.path + "|file", player.getFile());
		editor.putInt(folder.path + "|pos", player.getCurrentPosition());
		editor.apply();
	}

	private void setPref(String name, String val) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(name, val);
		editor.apply();
	}

	private void setPrefInt(String name, int val) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(name, val);
		editor.apply();
	}

	private void setPrefBool(String name, boolean val) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(name, val);
		editor.apply();
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

	private void startGps() {
		if (settings.getBoolean(getString(R.string.prefTestMode), false))
			return;
		if (!settings.getBoolean(getString(R.string.prefNavigatorMode), false))
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

		System.out.println("total no of songs are=" + mCursor.getCount());

		Map<String, List<File1>> map = new HashMap<String, List<File1>>();

		while (mCursor.moveToNext()) {
			String title = mCursor
					.getString(mCursor
							.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
			String path = mCursor.getString(mCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			long dur = mCursor.getLong(mCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
			//System.out.println(title + " " + path + " " + dur);

			String p = path.substring(path.indexOf('/', 1) + 1);
			for (int i = 0; i < p.length(); i++)
				if (p.charAt(i) == '/') {
					String fold = p.substring(0, i);
					if (!map.containsKey(fold))
						map.put(fold, new ArrayList<File1>());
				}

			String folder = p.substring(0, p.lastIndexOf('/'));

			List<File1> files = map.get(folder);
			files.add(new File1(path, title, dur));
		}
		mCursor.close();
		System.out.println();

		String fold = "000";
		List<File1> files1 = new ArrayList<File1>();
		files1.add(new File1("/sdcard0/000/audiocheck.net_sin_1000Hz_0dBFS_10s.wav", "audiocheck.net_sin_1000Hz_0dBFS_10s.wav", 100));
		files1.add(new File1("/sdcard0/000/audiocheck.net_sin_1000Hz_-6dBFS_10s.wav", "audiocheck.net_sin_1000Hz_-6dBFS_10s.wav", 100));
		files1.add(new File1("/sdcard0/000/audiocheck.net_sin_1000Hz_-20dBFS_10s.wav", "audiocheck.net_sin_1000Hz_-20dBFS_10s.wav", 100));
		map.put(fold, files1);
		
		
		List<String> folds = new ArrayList<String>(map.keySet());
		Collections.sort(folds);

		for (String folder : folds) {
			int ind = 0;
			for (int i = 0; i < folder.length(); i++)
				if (folder.charAt(i) == '/')
					ind++;

			List<File1> files = map.get(folder);
			Collections.sort(files);

			folders.add(new Folder(folder, ind, files.toArray(new File1[0])));
		}

		return folders;
	}

	void setMaxVolume() {
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		System.out.println("maxVol " + maxVol);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
	}
}