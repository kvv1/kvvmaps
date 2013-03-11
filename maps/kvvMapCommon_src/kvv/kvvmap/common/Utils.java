package kvv.kvvmap.common;

import kvv.kvvmap.adapter.Adapter;

public class Utils {
	public final static int MAX_ZOOM = 15;
	public final static int MIN_ZOOM = 1;

	public static final int TILE_SIZE_G = 256;

	public static double lon2x(double lon, int zoomLevel) {
		long size = (1L << zoomLevel) * TILE_SIZE_G; // - размер карты
																// на уровне
		// детализации zoomLevel в
		// пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
								// пикселях;

		double x = center + size * (lon / 360); // - координата x точки
		// находящейся на долготе Lon;
		return x * Adapter.TILE_SIZE / TILE_SIZE_G;
	}

	public static double lat2y(double lat, int zoomLevel) {
		long size = (1L << zoomLevel) * TILE_SIZE_G; // - размер карты
																// на уровне
		// детализации zoomLevel в пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
		// пикселях;

		double ls = Math.sin(lat * Math.PI / 180); // - синус широты;
		double y = center - 0.5 * Math.log((1 + ls) / (1 - ls))
				* (size / (2 * Math.PI)); // - координата y точки находящейся на
											// широте Lat;
		return y * Adapter.TILE_SIZE / TILE_SIZE_G;
	}

	public static double x2lon(double x, int zoomLevel) {
		x = x * TILE_SIZE_G / Adapter.TILE_SIZE;
		long size = (1L << zoomLevel) * TILE_SIZE_G; // - размер карты
																// на уровне
		// детализации zoomLevel в пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
		// пикселях;
		return (x - center) * 360 / size; // - долгота точки с координатой
	}

	public static double y2lat(double y, int zoomLevel) {
		y = y * TILE_SIZE_G / Adapter.TILE_SIZE;
		long size = (1L << zoomLevel) * TILE_SIZE_G; // - размер карты
																// на уровне
		// детализации zoomLevel в пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
		// пикселях;
		return (180 / Math.PI)
				* (2 * Math.atan(Math.exp((center - y) * 2 * Math.PI / size)) - Math.PI / 2); // широта.
	}

	public static String formatLatLon(double latLon) {
		String sign = "";
		if(latLon < 0) {
			sign = "-";
			latLon = -latLon;
		}
		
		int degrees = (int) latLon;
		int minutes = ((int) (latLon * 60)) % 60;
		int seconds = ((int) (latLon * 3600)) % 60;
		return String.format("%s%02d\u00B0%02d'%02d\"", sign, degrees, minutes, seconds);
	}

	public static  String formatDistance(int dist) {
		if (dist < 1000)
			return dist + "m";
		else if (dist < 10000)
			return String.format("%d.%02dkm", dist / 1000, (dist % 1000) / 10);
		else
			return dist / 1000 + "km";
	}

	public static long mkLong(int hi, int lo) {
		return (((long) hi) << 32) | (((long) lo) & 0xFFFFFFFF);
	}

	public static int getHi(long l) {
		return (int) (l >> 32);
	}

	public static int getLo(long l) {
		return (int) l;
	}

	public static int[] getGammaTable(double gamma) {
		int[] gammaTable = new int[256];
		for (int i = 0; i < 256; ++i) {
			gammaTable[i] = (int) Math.min(255,
					(int) ((255.0 * Math.pow(i / 255.0, 1.0 / gamma)) + 0.5));
		}
		return gammaTable;
	}

}
