package kvv.kvvmap.service;

import java.io.File;
import java.io.IOException;

import kvv.kvvmap.MapLoader;
import kvv.kvvmap.MyActivity;
import kvv.kvvmap.R;
import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.maps.MapDir;
import kvv.kvvmap.maps.MapsDir;
import kvv.kvvmap.placemark.Paths;
import kvv.kvvmap.placemark.PlaceMarks;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.smartbean.androidutils.service.BaseService;

public class KvvMapsService extends BaseService {

	public KvvMapsService() {
		super(R.drawable.icon, R.drawable.icon, MyActivity.class,
				R.string.app_name, R.string.app_name, false, true);
	}

	public interface KvvMapsServiceListener {
		void mapsLoaded();
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

	private void _add(final MapDir[] dirs, final String name) {
		h.post(new Runnable() {
			@Override
			public void run() {
				if (mapsDir != null && dirs != null)
					mapsDir.addMap(dirs, name);
			}
		});
	}

	private void loadMaps() {
		loadingMaps = true;
		new Thread() {
			{
				setPriority(NORM_PRIORITY);
			}

			public void run() {
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

				for (File file : files) {
					MapDir[] dirs;
					try {
						dirs = MapsDir.read(file);
						_add(dirs, MapsDir.getName(file));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				h.post(new Runnable() {
					@Override
					public void run() {
						loadingMaps = false;
						if (listener != null)
							listener.mapsLoaded();
					}
				});
			};
		}.start();

	}

	@Override
	public void onCreate() {
		Adapter.log("service onCreate");
		super.onCreate();
		h = new Handler();
		mapsDir = new MapsDir();

		loadMaps();
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

		super.onDestroy();
		System.gc();
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~KvvMapsService");
		super.finalize();
	}

}
