package kvv.kvvmap.service;

import java.io.File;
import java.io.IOException;

import kvv.kvvmap.MapLoader;
import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.common.maps.MapDir;
import kvv.kvvmap.common.maps.MapsDir;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

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
			paths.setDoc(null);
			placeMarks.setDoc(null);
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

	@Override
	public void onCreate() {

		h = new Handler();

		setForeground(true);
		Adapter.log("service onCreate");
		super.onCreate();

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
			placeMarks.setDoc(null);
		placeMarks = null;
		if (paths != null)
			paths.setDoc(null);
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
}
