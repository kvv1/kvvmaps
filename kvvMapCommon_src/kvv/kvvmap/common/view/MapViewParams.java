package kvv.kvvmap.common.view;

import kvv.kvvmap.common.Utils;

public final class MapViewParams {
	private double centerX;
	private double centerY;
	private int zoom = Utils.MIN_ZOOM;
	private double angle;
	private double cos;
	private double sin;
	
	public int getZoom() {
		return zoom;
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

	{
		setAngle(Math.PI / 8);
		setAngle(Math.PI / 16);
		setAngle(0);
	}

	public void setZoom(int zoom) {
		double lon = Utils.x2lon(centerX, this.zoom);
		double lat = Utils.y2lat(centerY, this.zoom);
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
	}

	public void animateBy(double dx, double dy) {
		double x = centerX + dx;
		double y = centerY + dy;
		double lat = Utils.y2lat(y, zoom);
		if (lat > 85 || lat < -85)
			return;
		centerX = x;
		centerY = y;
	}

	public double centerX() {
		return centerX;
	}

	public double centerY() {
		return centerY;
	}

	public double lon() {
		return Utils.x2lon(centerX, this.zoom);
	}

	public double lat() {
		return Utils.y2lat(centerY, this.zoom);
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
	
	
}
