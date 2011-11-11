xaxa
package yandex.util;

//https://code.google.com/hosting/settings
	
public class YandexUtils {


	public static double lat2y_yandex(double lon, int zoom) {
		double m = lon * Math.PI / 180;
		double k = 0.0818191908426;
		double e = 6378137 * Math.log(Math.tan(Math.PI / 4 + m / 2)
				/ Math.pow(
						Math.tan(Math.PI / 4 + Math.asin(k * Math.sin(m)) / 2),
						k));
		double y = (20037508.342789 - e) * 53.5865938;
		y = Math.max(Math.min(y, 2147483647), 0);
		return (long) y >> (23 - zoom);
	}

	public static double y2lat_yandex(double y, int zoom) {
		long yy = (long) y << (23 - zoom);
		double a = 20037508.342789 - yy / 53.5865938;
		double g = (Math.PI / 2) - 2
				* Math.atan(1 / Math.exp(a / (double) 6378137));
		double l = g + 0.003356551468879694 * Math.sin(2 * g)
				+ 0.00000657187271079536 * Math.sin(4 * g) + 1.764564338702e-8
				* Math.sin(6 * g) + 5.328478445e-11 * Math.sin(8 * g);
		double r = l * 180 / Math.PI;
		return r;
	}

	public static void main(String[] args) {

		double lat = 60.039;

		int y_y = (int) lat2y_yandex(lat, 14);
		int y_g = (int) lat2y(lat, 14);

		System.out.println(y_y / 256 + " " + y_y);
		System.out.println(y_g / 256 + " " + y_g);

		System.out.println(y2lat_yandex(y_y, 14));
	}

	// /////////////////////////////////////////////////////////////
	// GOOGLE

	public static double lon2x(double lon, int zoomLevel) {
		long size = (1L << zoomLevel) * 256; // - размер карты на уровне
												// детализации zoomLevel в
												// пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
								// пикселях;

		double x = center + size * (lon / 360); // - координата x точки
		// находящейся на долготе Lon;
		return x;
	}

	public static double lat2y(double lat, int zoomLevel) {
		long size = (1L << zoomLevel) * 256; // - размер карты на уровне
		// детализации zoomLevel в пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
		// пикселях;

		double ls = Math.sin(lat * Math.PI / 180); // - синус широты;
		double y = center - 0.5 * Math.log((1 + ls) / (1 - ls))
				* (size / (2 * Math.PI)); // - координата y точки находящейся на
											// широте Lat;
		return y;
	}

	public static double x2lon(double x, int zoomLevel) {
		long size = (1L << zoomLevel) * 256; // - размер карты на уровне
		// детализации zoomLevel в пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
		// пикселях;
		return (x - center) * 360 / size; // - долгота точки с координатой
	}

	public static double y2lat(double y, int zoomLevel) {
		long size = (1L << zoomLevel) * 256; // - размер карты на уровне
		// детализации zoomLevel в пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
		// пикселях;
		return (180 / Math.PI)
				* (2 * Math.atan(Math.exp((center - y) * 2 * Math.PI / size)) - Math.PI / 2); // широта.
	}

}
