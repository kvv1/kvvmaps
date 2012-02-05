package kvv.kvvmap.adapter;

import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.pacemark.ISelectable;
import android.location.Location;

public class LocationX implements ISelectable {

	private static final float[] distResults = new float[2];

	public volatile String name;

	private double lon;
	private double lat;
	private double alt;
	private long time;
	private float bearing;
	private float acc;
	private float speed;

	private int z = -1;
	private double x;
	private double y;

	public LocationX(double lon, double lat) {
		this(lon, lat, 0, 0, 0, 0);
	}

	public LocationX(double lon, double lat, double alt, float acc,
			float speed, long time) {
		this.lon = lon;
		this.lat = lat;
		this.alt = alt;
		this.acc = acc;
		this.speed = speed;
		this.time = time;
	}

	public LocationX(Location loc) {
		this(loc.getLongitude(), loc.getLatitude(), loc.getAltitude(), loc
				.getAccuracy(), loc.getSpeed(), loc.getTime());
		bearing = loc.getBearing();
		if (!loc.hasSpeed() || loc.getSpeed() > 120)
			speed = -1;
	}

	public synchronized double getX(int zoom) {
		calcXY(zoom);
		return x;
	}

	public synchronized double getY(int zoom) {
		calcXY(zoom);
		return y;
	}

	public synchronized int getXint(int zoom) {
		calcXY(zoom);
		return (int) x;
	}

	public synchronized int getYint(int zoom) {
		calcXY(zoom);
		return (int) y;
	}

	private void calcXY(int zoom) {
		if (z != zoom) {
			y = Utils.lat2y(lat, zoom);
			x = Utils.lon2x(lon, zoom);
			z = zoom;
		}
	}

	public double getLongitude() {
		return lon;
	}

	public double getLatitude() {
		return lat;
	}

	public double getAltitude() {
		return alt;
	}

	public float getAccuracy() {
		return acc;
	}

	public float getSpeed() {
		return speed;
	}

	public long getTime() {
		return time;
	}

	public float getBearing() {
		return bearing;
	}

	public float distanceTo(LocationX dest) {
		synchronized (distResults) {
			Location.distanceBetween(lat, lon, dest.lat, dest.lon, distResults);
			return distResults[0];
		}
	}

	public float bearingTo(LocationX dest) {
		synchronized (distResults) {
			Location.distanceBetween(lat, lon, dest.lat, dest.lon, distResults);
			return distResults[1];
		}
	}
}
