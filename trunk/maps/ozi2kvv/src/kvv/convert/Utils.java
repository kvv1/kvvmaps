package kvv.convert;

import java.util.Arrays;

public class Utils {
	public static double lon2x(double lon, int zoomLevel) {
		long size = (1L << zoomLevel) * 256; // - размер карты на уровне
												// детализации zoomLevel в
												// пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
								// пикселях;

		return center + size * (lon / 360); // - координата x точки
											// находящейся на долготе Lon;
	}

	public static double lat2y(double lat, int zoomLevel) {
		long size = (1L << zoomLevel) * 256; // - размер карты на уровне
		// детализации zoomLevel в пикселях;
		long center = size / 2; // - задаёт x и y координаты центра карты в
		// пикселях;

		double ls = Math.sin(lat * Math.PI / 180); // - синус широты;
		return center - 0.5 * Math.log((1 + ls) / (1 - ls))
				* (size / (2 * Math.PI)); // - координата y точки находящейся на
											// широте Lat;
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

	public static String format(double latLon) {
		int degrees = (int) latLon;
		int minutes = ((int) (latLon * 60)) % 60;
		int seconds = ((int) (latLon * 3600)) % 60;
		return "" + degrees + "-" + minutes + "-" + seconds;
	}

	public static Double parse(String val) throws NumberFormatException {
		double res = 0;

		try {
			return Double.parseDouble(val);
		} catch (NumberFormatException e) {
		}

		try {
			String[] vals = val.split("-", -1);
			if (vals.length > 0)
				res += Integer.parseInt(vals[0]);
			if (vals.length > 1)
				res += Double.parseDouble(vals[1]) / 60;
			if (vals.length > 2)
				res += Double.parseDouble(vals[2]) / 3600;
		} catch (NumberFormatException e) {
			System.err.println("error parsing " + val);
			throw e;
		}
		return res;
	}

	static class PoligonBorder {
		private int ymin;
		private int ymax;
		private int[][] data;

		public PoligonBorder(int[] xp, int[] yp) {
			ymin = yp[0];
			ymax = yp[0];
			for (int y : yp) {
				if (y < ymin)
					ymin = y;
				if (y > ymax)
					ymax = y;
			}

			int npol = xp.length;

			data = new int[ymax - ymin][];

			int[] temp = new int[npol];

			for (int y = ymin; y < ymax; y++) {
				int n = 0;
				for (int i = 0, j = npol - 1; i < npol; j = i++) {
					if ((((yp[i] <= y) && (y < yp[j])) || ((yp[j] <= y) && (y < yp[i])))) {
						int x = (xp[j] - xp[i]) * (y - yp[i]) / (yp[j] - yp[i])
								+ xp[i];
						temp[n++] = x;
					}
				}
				data[y - ymin] = Arrays.copyOf(temp, n);
			}
		}
		
		public boolean test(int x, int y) {
			if(y < ymin || y >= ymax)
				return false;
			boolean c = false;
			for(int x1 : data[y - ymin]) {
				if(x > x1)
					c = !c;
			}
			return c;
		}
	}

	public static boolean pointInsidePoligon(int[] xp, int[] yp, int x, int y) {
		int npol = xp.length;
		boolean c = false;
		for (int i = 0, j = npol - 1; i < npol; j = i++) {
			if ((((yp[i] <= y) && (y < yp[j])) || ((yp[j] <= y) && (y < yp[i])))
					&& (x > (xp[j] - xp[i]) * (y - yp[i]) / (yp[j] - yp[i])
							+ xp[i]))
				c = !c;
		}
		return c;
	}

}
