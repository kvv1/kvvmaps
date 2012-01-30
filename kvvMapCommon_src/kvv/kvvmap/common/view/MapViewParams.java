package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.Utils;

public final class MapViewParams {
	private double centerX;
	private double centerY;
	private int zoom = Utils.MIN_ZOOM;
	private int prevzoom = Utils.MIN_ZOOM;
	private double angle;
	private double cos;
	private double sin;

	{
		setAngle(Math.PI / 8);
		setAngle(Math.PI / 16);
		setAngle(0);
	}


	private LocationX loc = new LocationX(0, 0);

	public int getZoom() {
		return zoom;
	}

	public int getPrevZoom() {
		return prevzoom;
	}
	
	// public PointD geo2scr(PointD geo) {
	// double x = geo.x - centerX;
	// double y = geo.y - centerY;
	// return new PointD(x, y);
	// }

	public double geo2scrX(double x, double y) {
		x -= centerX;
		y -= centerY;
		return x * cos - y * sin;
	}

	public double geo2scrY(double x, double y) {
		x -= centerX;
		y -= centerY;
		return x * sin + y * cos;
	}

	public double scr2geoX(double x, double y) {
		double x1 = x * cos + y * sin;
		return x1 + centerX;
	}

	public double scr2geoY(double x, double y) {
		double y1 = -x * sin + y * cos;
		return y1 + centerY;
	}

	public double scrX2lon(double x, double y) {
		return Utils.x2lon(scr2geoX(x, y), zoom);
	}

	public double scrY2lat(double x, double y) {
		return Utils.y2lat(scr2geoY(x, y), zoom);
	}

	public void setAngle(double angle) {
		this.angle = angle;
		this.cos = Math.cos(angle);
		this.sin = Math.sin(angle);
	}

	public void setZoom(int zoom) {
		prevzoom = this.zoom;
		double lon = loc.getLongitude();
		double lat = loc.getLatitude();
		this.zoom = zoom;
		animateTo(lon, lat, 0, 0);
	}

	public void animateTo(double lon, double lat, int dx, int dy) {
		if (lat > 85 || lat < -85)
			return;
		double x = Utils.lon2x(lon, zoom);
		double y = Utils.lat2y(lat, zoom);
		centerX = x - dx;
		centerY = y - dy;
		loc = new LocationX(lon, lat);
	}

	public void animateBy(double dx, double dy) {
		double x = scr2geoX(dx, dy);
		double y = scr2geoY(dx, dy);
		double lon = Utils.x2lon(x, zoom);
		double lat = Utils.y2lat(y, zoom);
		if (lat > 85 || lat < -85)
			return;
		centerX = x;
		centerY = y;
		loc = new LocationX(lon, lat);
	}

	public double centerX() {
		return centerX;
	}

	public double centerY() {
		return centerY;
	}

	public double sin() {
		return sin;
	}

	public double cos() {
		return cos;
	}

	public double angle() {
		return angle;
	}

	public LocationX getLocation() {
		return loc;
	}

}
