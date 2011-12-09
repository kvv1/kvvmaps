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

	private LocationManager lm;
	private TrackerListener listener;

	public interface TrackerListener {
	}

	public Tracker(Paths paths, LocationManager locationManager) {
		this.lm = locationManager;
		this.paths = paths;
	}

	public void setListener(TrackerListener tl) {
		listener = tl;
	}
	
	public void startPath() {
		if (curPath == null) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 20.0f,
					this);
			curPath = paths.createPath(null);
		}
	}

	public void endPath() {
		if (curPath != null)
			curPath.finishCompact();
		curPath = null;
		lm.removeUpdates(this);
	}

	public boolean isTracking() {
		return curPath != null;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			if (curPath != null && location.getAccuracy() < 50) {
				curPath.addCompact(new LocationX(location));
//				listener.setMyLocation(new LocationX(location), false);
			}
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

	public void dispose() {
		setListener(null);
		endPath();
		this.lm = null;
	}

}
