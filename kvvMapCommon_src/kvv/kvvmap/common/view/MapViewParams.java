package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.Utils;

public class MapViewParams {
	private double centerX;
	private double centerY;
	private int zoom;
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

	public void animateBy(PointInt offset) {
		double x = centerX + offset.x;
		double y = centerY + offset.y;
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
