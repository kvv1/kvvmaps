package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.Utils;

public final class MapViewParams {
//	private double centerX;
//	private double centerY;
	private int zoom = Utils.MIN_ZOOM;
	private int prevzoom = Utils.MIN_ZOOM;
	private float angle;
	private double cos;
	private double sin;
	private LocationX loc = new LocationX(0, 0);


	{
		setAngle(0);
		// setAngle(45);
		// setAngle(90);
	}

	public int getZoom() {
		return zoom;
	}

	public int getPrevZoom() {
		return prevzoom;
	}

	public double loc2scrX(LocationX loc) {
		return geo2scrX(loc.getX(zoom),
				loc.getY(zoom));
	}
	
	public double loc2scrY(LocationX loc) {
		return geo2scrY(loc.getX(zoom),
				loc.getY(zoom));
	}
	
	public double geo2scrX(double x, double y) {
		x -= centerX();
		y -= centerY();
		return x * cos - y * sin;
	}

	public double geo2scrY(double x, double y) {
		x -= centerX();
		y -= centerY();
		return x * sin + y * cos;
	}

	public double scr2geoX(double x, double y) {
		double x1 = x * cos + y * sin;
		return x1 + centerX();
	}

	public double scr2geoY(double x, double y) {
		double y1 = -x * sin + y * cos;
		return y1 + centerY();
	}

	public double scr2lon(double x, double y) {
		return Utils.x2lon(scr2geoX(x, y), zoom);
	}

	public double scr2lat(double x, double y) {
		return Utils.y2lat(scr2geoY(x, y), zoom);
	}

	public void setAngle(float deg) {
		this.angle = deg;
		this.cos = Math.cos(deg * Math.PI / 180);
		this.sin = Math.sin(deg * Math.PI / 180);
	}

	public void setZoom(int zoom) {
		prevzoom = this.zoom;
//		double lon = loc.getLongitude();
//		double lat = loc.getLatitude();
		this.zoom = zoom;
//		animateTo(lon, lat);
	}
	
	public void animateTo(double lon, double lat) {
		if (lat > 85 || lat < -85)
			return;
//		centerX = Utils.lon2x(lon, zoom);
//		centerY = Utils.lat2y(lat, zoom);
		loc = new LocationX(lon, lat);
	}

	public void scrollBy(double dx, double dy) {
		double x = scr2geoX(dx, dy);
		double y = scr2geoY(dx, dy);
		double lon = Utils.x2lon(x, zoom);
		double lat = Utils.y2lat(y, zoom);
		if (lat > 85 || lat < -85)
			return;
//		centerX = x;
//		centerY = y;
		loc = new LocationX(lon, lat);
	}

	public double centerX() {
		return loc.getX(zoom);
	}

	public double centerY() {
		return loc.getY(zoom);
	}

	public double sin() {
		return sin;
	}

	public double cos() {
		return cos;
	}

	public float angle() {
		return angle;
	}

	public LocationX getLocation() {
		return loc;
	}

}
