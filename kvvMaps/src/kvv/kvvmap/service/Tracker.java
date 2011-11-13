package kvv.kvvmap.service;

import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.pacemark.Path;
import kvv.kvvmap.common.pacemark.Paths;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Tracker implements LocationListener {
	private final Paths paths;
	private Path curPath;
	private boolean following;

	private LocationManager lm;
	private TrackerListener listener;

	public interface TrackerListener {
		void dimmMyLocation();
		void setMyLocation(LocationX locationX, boolean forceScroll);
	}
	
	public Tracker(TrackerListener listener, Paths paths) {
		this.paths = paths;
		this.listener = listener;
	}

	private void _register() {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 20.0f,
				this);
	}

	private void _unregister() {
		lm.removeUpdates(this);
		listener.dimmMyLocation();
	}

	public void startPath() {
		if (curPath == null && !following)
			_register();
		if (curPath == null)
			curPath = paths.createPath(null);
	}

	public void endPath() {
		if (curPath != null)
			curPath.finishCompact();

		curPath = null;
		if (curPath == null && !following)
			_unregister();
	}

	public void startFollow() {
		if (curPath == null && !following)
			_register();
		following = true;
		listener.setMyLocation(null, false);
	}

	public void stopFollow() {
		following = false;
		if (curPath == null && !following)
			_unregister();
	}

	public boolean isTracking() {
		return curPath != null;
	}

	public boolean isFollowing() {
		return following;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			if (curPath != null && location.getAccuracy() < 50)
				curPath.addCompact(new LocationX(location));

			if (curPath != null || following)
				listener.setMyLocation(new LocationX(location), false);
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	public void register(LocationManager lm) {
		this.lm = lm;
	}

	public void unregister() {
		endPath();
		stopFollow();
		this.lm = null;
	}

}
