package kvv.kvvmap.common.view;

import kvv.kvvmap.common.Utils;

public final class MapViewParams {
	private double centerX;
	private double centerY;
	private int zoom = Utils.MIN_ZOOM;
	private float angle;

	public int getZoom() {
		return zoom;
	}

	public double geo2scrX(double x) {
		return x - centerX;
	}

	public double geo2scrY(double y) {
		return y - centerY;
	}

	public double scr2geoX(double x) {
		return x + centerX;
	}

	public double scr2geoY(double y) {
		return y + centerY;
	}

	public double lon2scrX(double lon) {
		return geo2scrX(Utils.lon2x(lon, zoom));
	}

	public double lat2scrY(double lat) {
		return geo2scrY(Utils.lat2y(lat, zoom));
	}

	public double scrX2lon(double x) {
		return Utils.x2lon(scr2geoX(x), zoom);
	}

	public double scrY2lat(double y) {
		return Utils.y2lat(scr2geoY(y), zoom);
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
}
