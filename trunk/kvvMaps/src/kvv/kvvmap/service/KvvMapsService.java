package kvv.kvvmap.service;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.maps.MapsDir;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import kvv.kvvmap.service.Tracker.TrackerListener;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class KvvMapsService extends Service {

	public interface IKvvMapsService {
		Tracker getTracker();

		Paths getPaths();

		void setTrackerListener(TrackerListener tl);

		MapsDir getMapsDir();

		PlaceMarks getPlacemarks();

		void setBundle(Bundle outState);
		Bundle getBundle();
		
		void disconnect();
	}

	private final KvvMapsServiceBinder myServiceBinder = new KvvMapsServiceBinder();
	private Tracker tracker;
	private Paths paths;
	private PlaceMarks placeMarks;
	private MapsDir mapsDir;
	private volatile TrackerListener trackerListener;
	private Bundle state;

	public class KvvMapsServiceBinder extends Binder implements IKvvMapsService {
		public Tracker getTracker() {
			return tracker;
		}

		@Override
		public Paths getPaths() {
			return paths;
		}

		@Override
		public void setTrackerListener(TrackerListener tl) {
			KvvMapsService.this.trackerListener = tl;
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
		public void setBundle(Bundle outState) {
			state = outState;
		}

		@Override
		public Bundle getBundle() {
			return state;
		}

		@Override
		public void disconnect() {
			trackerListener = null;
			paths.setDoc(null);
			placeMarks.setDoc(null);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return myServiceBinder;
	}

	static class TTT {
		@Override
		protected void finalize() throws Throwable {
			Adapter.log("~TTT");
			super.finalize();
		}
	}
	
	@Override
	public void onCreate() {
		Adapter.log("service onCreate");
		super.onCreate();
		mapsDir = new MapsDir();

		new TTT();
		
		placeMarks = new PlaceMarks();

		paths = new Paths();
		tracker = new Tracker(new TrackerListener() {

			@Override
			public void setMyLocation(LocationX locationX, boolean forceScroll) {
				TrackerListener tl = KvvMapsService.this.trackerListener;
				if (tl != null)
					tl.setMyLocation(locationX, forceScroll);
			}

			@Override
			public void dimmMyLocation() {
				TrackerListener tl = KvvMapsService.this.trackerListener;
				if (tl != null)
					tl.dimmMyLocation();
			}
		}, paths);
		tracker.register((LocationManager) getSystemService(Context.LOCATION_SERVICE));
	}

	@Override
	public void onDestroy() {
		Adapter.log("service onDestroy");
		if (placeMarks != null)
			placeMarks.setDoc(null);
		placeMarks = null;
		if (paths != null)
			paths.setDoc(null);
		paths = null;
		mapsDir = null;
		if (tracker != null)
			tracker.unregister();
		tracker = null;
		trackerListener = null;
		state = null;
		System.gc();
		
		System.runFinalizersOnExit(true);
		System.gc();
		//System.exit(0);
		
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~KvvMapsService");
		super.finalize();
	}
}
