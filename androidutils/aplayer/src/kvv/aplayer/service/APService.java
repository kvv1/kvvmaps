package kvv.aplayer.service;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kvv.aplayer.APActivity;
import kvv.aplayer.MemoryStorage;
import kvv.aplayer.R;
import kvv.aplayer.RemoteControlReceiver;
import kvv.aplayer.folders.Folder;
import kvv.aplayer.player.Player1;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.smartbean.androidutils.service.BaseService;
import com.smartbean.androidutils.util.StorageUtils;
import com.smartbean.androidutils.util.StorageUtils.StorageInfo;

public class APService extends BaseService {
	public final static File ROOT = new File(
			Environment.getExternalStorageDirectory(), "/external_sd/aplayer/");

	public static final float[] dBPer100kmh = { 0, 5f };
	public static final int[] compr = { 0, 6, 10 };

	private Set<APServiceListener> listeners = new HashSet<APServiceListener>();

	private Player1 player;

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
			player.setVolume(1f);

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
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(APService.this);
				if (settings.getBoolean(getString(R.string.prefNavigatorMode),
						false))
					player.pause();
			}
		}
	};

	public APService() {
		super(R.drawable.ic_launcher, R.drawable.ic_launcher, APActivity.class,
				R.string.app_name, R.string.app_name, false, false);
	}

	Runnable visEnabler = new Runnable() {
		@Override
		public void run() {
			player.enVis();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		// //////////////////////////////////////

		System.out.println("---");
		for (StorageInfo info : StorageUtils.getStorageList())
			System.out.println(info.path);
		System.out.println("---");
		for (String key : MemoryStorage.getAllStorageLocations().keySet())
			System.out.println(key + " "
					+ MemoryStorage.getAllStorageLocations().get(key));
		System.out.println("---");
		for (String s : StorageUtils.getStorageDirectories())
			System.out.println(s);
		System.out.println("---");

		String[] ll = new File("/storage/sdcard1").list();
		if (ll != null)
			for (String f : ll)
				System.out.println(f);
		System.out.println("---");

		// //////////////////////////////////////

		settings = PreferenceManager.getDefaultSharedPreferences(this);

		List<Folder> folders = new ArrayList<Folder>();

		if (Build.VERSION.SDK_INT < 14)
			for (StorageInfo info : StorageUtils.getStorageList())
				read(new File(info.path), folders, 0);
		else
			for (String s : StorageUtils.getStorageDirectories())
				read(new File(s), folders, 0);

		// readFolders(ROOT, 0, folders);

		player = new Player1(folders) {
			@Override
			protected void onChanged() {
				System.out.println("onChanged " + isPlaying());

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
					l.onChanged();

				handler.removeCallbacks(visEnabler);
				handler.postDelayed(visEnabler, 200);
			}

			@Override
			protected void onRandomChanged() {
				for (APServiceListener l : listeners)
					l.onRandomChanged();
			}

		};

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

	@Override
	public void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		player.close();
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		stopGpsRunnable.run();
		super.onDestroy();
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

	}

	private static int read(File dir, List<Folder> folders, int indent) {
		File[] files = listFiles(dir);
		File[] dirs = listDirs(dir);

		int sum = 0;

		if (files != null)
			sum = files.length;

		List<Folder> folders2 = new ArrayList<Folder>();

		if (dirs != null)
			for (File d : dirs)
				if (indent != 0 || !d.getName().equals("external_sd"))
					sum += read(d, folders2, indent + 1);

		if (sum > 0) {
			folders.add(new Folder(dir.getAbsolutePath(), indent, files));
			folders.addAll(folders2);
		}

		return sum;
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
		if (res != null)
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
		if (res != null)
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

}