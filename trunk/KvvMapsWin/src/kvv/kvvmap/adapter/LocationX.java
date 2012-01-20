package kvv.kvvmap.adapter;

import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.pacemark.ISelectable;

public class LocationX implements ISelectable {
	private final double lon;
	private final double lat;
	private final double alt;
	private final float acc;
	private final float speed;
	private final long time;

	public volatile String name;

	public LocationX(double lon, double lat, double alt, float acc,
			float speed, long time) {
		this.lon = lon;
		this.lat = lat;
		this.alt = alt;
		this.acc = acc;
		this.speed = speed;
		this.time = time;
	}

	public LocationX(double lon, double lat) {
		this(lon, lat, 0, 0, 0, 0);
	}

	public int getXint(int zoom) {
		return (int) Utils.lon2x(lon, zoom);
	}

	public int getYint(int zoom) {
		return (int) Utils.lat2y(lat, zoom);
	}

	public double getX(int zoom) {
		return Utils.lon2x(lon, zoom);
	}

	public double getY(int zoom) {
		return Utils.lat2y(lat, zoom);
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

	public double getAccuracy() {
		return acc;
	}

	public float getSpeed() {
		return speed;
	}

	public long getTime() {
		return time;
	}

	public double distanceTo(LocationX loc) {
		double dy = (loc.getLatitude() - lat) * 111000;
		double dx = (loc.getLongitude() - lon) * 111000
				* Math.cos(loc.getLatitude() * Math.PI / 180);
		return Math.sqrt(dy * dy + dx * dx);
	}

	public float getBearing() {
		return 0;
	}

	public float bearingTo(LocationX loc) {
		double dy = (loc.getLatitude() - lat) * 111000;
		double dx = (loc.getLongitude() - lon) * 111000
				* Math.cos(loc.getLatitude() * Math.PI / 180);
		return (float) (90 - (Math.atan2(dy, dx) * 180 / Math.PI));
	}
}