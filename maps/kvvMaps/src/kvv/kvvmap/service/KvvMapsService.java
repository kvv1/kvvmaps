package kvv.kvvmap.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import kvv.kvvmap.FakeActivity;
import kvv.kvvmap.MapLoader;
import kvv.kvvmap.R;
import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.common.maps.MapDir;
import kvv.kvvmap.common.maps.MapsDir;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class KvvMapsService extends Service {

	public interface KvvMapsServiceListener {
		void mapsLoaded();
	}

	public interface IKvvMapsService {
		Tracker getTracker();

		Paths getPaths();

		MapsDir getMapsDir();

		PlaceMarks getPlacemarks();

		Bundle getBundle();

		boolean isLoadingMaps();

		void disconnect();

		void setListener(KvvMapsServiceListener l);
	}

	private final KvvMapsServiceBinder myServiceBinder = new KvvMapsServiceBinder();
	private Tracker tracker;
	private Paths paths;
	private PlaceMarks placeMarks;
	private MapsDir mapsDir;
	private Bundle state;
	private KvvMapsServiceListener listener;

	public class KvvMapsServiceBinder extends Binder implements IKvvMapsService {
		public Tracker getTracker() {
			return tracker;
		}

		@Override
		public Paths getPaths() {
			return paths;
		}

		@Override
		public MapsDir getMapsDir() {
			return mapsDir;
		}

		@Override
		public PlaceMarks getPlacemarks() {
			return placeMarks;
		}

		@Override
		public Bundle getBundle() {
			if (state == null)
				state = new Bundle();
			return state;
		}

		@Override
		public void disconnect() {
			mapsDir.setListener(null);
			paths.setListener(null);
			placeMarks.setListener(null);
			KvvMapsService.this.listener = null;
		}

		@Override
		public boolean isLoadingMaps() {
			return loadingMaps;
		}

		@Override
		public void setListener(KvvMapsServiceListener l) {
			KvvMapsService.this.listener = l;
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return myServiceBinder;
	}

	private Handler h;
	private boolean loadingMaps;

	class A implements Runnable {

		private File[] files;
		int idx;

		A(File[] files) {
			this.files = files;
			loadingMaps = true;
		}

		@Override
		public void run() {
			if (idx == files.length) {
				loadingMaps = false;
				Adapter.log("loadingMaps = false");
				if (listener != null)
					listener.mapsLoaded();
				return;
			}

			final File file = files[idx++];

			new Thread() {
				{
					setPriority((MIN_PRIORITY + MAX_PRIORITY) / 2);
				}

				public void run() {
					try {
						final MapDir[] dirs = MapsDir.read(file);
						h.post(new Runnable() {
							@Override
							public void run() {
								if (mapsDir != null && dirs != null) {
									mapsDir.addMap(dirs, mapsDir.getName(file));
								}
								h.post(A.this);
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	@SuppressWarnings("rawtypes")
	private static final Class[] mStartForegroundSignature = new Class[] {
			int.class, Notification.class };
	@SuppressWarnings("rawtypes")
	private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };

	private NotificationManager mNM;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	@Override
	public void onCreate() {

		h = new Handler();

		// setForeground(true);

		Adapter.log("service onCreate");
		super.onCreate();

		// ++++++++++++++++++++++++++++++++++++++++++++++++
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		try {
			mStartForeground = getClass().getMethod("startForeground",
					mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground",
					mStopForegroundSignature);
		} catch (NoSuchMethodException e) {
			// Running on an older platform.
			mStartForeground = mStopForeground = null;
		}

		Notification note = new Notification(R.drawable.icon_small, "",
				System.currentTimeMillis());
		note.flags |= Notification.FLAG_NO_CLEAR;

		Intent intent = new Intent(this, FakeActivity.class);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

		note.setLatestEventInfo(this, "KvvMaps", "", pi);

		startForegroundCompat(1337, note);

		// ++++++++++++++++++++++++++++++++++++++++++++++++

		File[] files = new File(Adapter.MAPS_ROOT).listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.getName().equals(MapLoader.DEFAULT_MAP_DIR_NAME)) {
				File t = files[0];
				files[i] = t;
				files[0] = file;
				break;
			}
		}

		// mapsDir = new MapsDir(files);

		mapsDir = new MapsDir();
		h.postDelayed(new A(files), 0);

		placeMarks = new PlaceMarks();

		paths = new Paths();
		tracker = new Tracker(paths,
				(LocationManager) getSystemService(Context.LOCATION_SERVICE));
	}

	@Override
	public void onDestroy() {
		Adapter.log("service onDestroy");

		h.removeCallbacksAndMessages(null);

		if (placeMarks != null)
			placeMarks.setListener(null);
		placeMarks = null;
		if (paths != null)
			paths.setListener(null);
		paths = null;
		if (mapsDir != null)
			mapsDir.setListener(null);
		mapsDir = null;
		if (tracker != null) {
			tracker.dispose();
		}
		tracker = null;
		state = null;
		listener = null;

		System.gc();
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~KvvMapsService");
		super.finalize();
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(int id, Notification notification) {
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(id);
			mStartForegroundArgs[1] = notification;
			try {
				mStartForeground.invoke(this, mStartForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w("MyApp", "Unable to invoke startForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w("MyApp", "Unable to invoke startForeground", e);
			}
			return;
		}

		// Fall back on the old API.
		setForeground(true);
		mNM.notify(id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	void stopForegroundCompat(int id) {
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w("MyApp", "Unable to invoke stopForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w("MyApp", "Unable to invoke stopForeground", e);
			}
			return;
		}

		// Fall back on the old API. Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		mNM.cancel(id);
		setForeground(false);
	}
}
