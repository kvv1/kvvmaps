package kvv.kvvmap.adapter;

import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.pacemark.ISelectable;
import android.location.Location;

public class LocationX implements ISelectable {
	private final Location loc;

	public volatile String name;
//	public volatile String descr;

	private double x;
	private double y;
	private int z = -1;
	private int bmsize;

	public LocationX(double lon, double lat) {
		this(lon, lat, 0, 0, 0, 0);
	}

	public LocationX(double lon, double lat, double alt, float acc,
			float speed, long time) {
		loc = new Location("");
		loc.setLongitude(lon);
		loc.setLatitude(lat);
		loc.setAltitude(alt);
		loc.setAccuracy(acc);
		loc.setSpeed(speed);
		loc.setTime(time);
	}

	public LocationX(Location loc) {
		this.loc = loc;
	}

	public synchronized int getX(int zoom) {
		calcXY(zoom);
		return (int) x;
	}

	public synchronized int getY(int zoom) {
		calcXY(zoom);
		return (int) y;
	}

	public synchronized double getXd(int zoom) {
		calcXY(zoom);
		return x;
	}

	public synchronized double getYd(int zoom) {
		calcXY(zoom);
		return y;
	}

	private void calcXY(int zoom) {
		if (z != zoom || bmsize != Adapter.TILE_SIZE) {
			y = Utils.lat2y(loc.getLatitude(), zoom);
			x = Utils.lon2x(loc.getLongitude(), zoom);
			z = zoom;
			bmsize = Adapter.TILE_SIZE;
		}
	}

	public double getLongitude() {
		return loc.getLongitude();
	}

	public double getLatitude() {
		return loc.getLatitude();
	}

	public double getAltitude() {
		return loc.getAltitude();
	}

	public float getAccuracy() {
		return loc.getAccuracy();
	}

	public float getSpeed() {
		return loc.getSpeed();
	}

	public long getTime() {
		return loc.getTime();
	}

	public float getBearing() {
		return loc.getBearing();
	}

	public float distanceTo(LocationX dest) {
		return loc.distanceTo(dest.loc);
	}
	
	public float bearingTo(LocationX dest) {
		return loc.bearingTo(dest.loc);
	}
}
