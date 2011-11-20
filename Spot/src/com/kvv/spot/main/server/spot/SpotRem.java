package com.kvv.spot.main.server.spot;


public class SpotRem {
//	Rectangle spot;
	int spotW;
	int spotH;

	int d;

	Doc doc;

	double[][] data;

	int ww;

	int hh;

	private boolean horiz;

	private boolean vert;

	SpotRem(Doc document, int diam, boolean horiz,
			boolean vert) {
		int w = document.getWidth();
		int h = document.getHeight();

		spotW = w - w % diam;
		spotH = h - h % diam;

		d = diam;
		doc = document;

		ww = w / d;
		hh = h / d;

		this.horiz = horiz;
		this.vert = vert;

		data = new double[ww][hh];
	}

	private static double interpolate(double x, double y, double f00,
			double f01, double f10, double f11) {
		return f00 * (1 - x) * (1 - y) + f10 * x * (1 - y) + f01 * (1 - x) * y
				+ f11 * x * y;
	}

	/*
	 * private int interpolatePixel(double x, double y, int f00, int f01, int
	 * f10, int f11) { double r = interpolate(x, y, doc.getR(f00),
	 * doc.getR(f01), doc .getR(f10), doc.getR(f11)); double g = interpolate(x,
	 * y, doc.getG(f00), doc.getG(f01), doc .getG(f10), doc.getG(f11)); double b =
	 * interpolate(x, y, doc.getB(f00), doc.getB(f01), doc .getB(f10),
	 * doc.getB(f11)); return doc.makePixel((int) r, (int) g, (int) b); }
	 */
	private static double interpolate(double x0, double x1, double x,
			double f0, double f1) {
		return f0 + (x - x0) * (f1 - f0) / (x1 - x0);
	}

	/*
	 * private int interpolatePixel(double x0, double x1, double x, int f0, int
	 * f1) { double r = interpolate(x0, x1, x, doc.getR(f0), doc.getR(f1));
	 * double g = interpolate(x0, x1, x, doc.getG(f0), doc.getG(f1)); double b =
	 * interpolate(x0, x1, x, doc.getB(f0), doc.getB(f1)); return
	 * doc.makePixel((int) r, (int) g, (int) b); }
	 */
	double getGain(int x, int y) {
		if (x < d / 2 || x >= spotW - d / 2 || y < d / 2
				|| y >= spotH - d / 2)
			return 1;

		int nx = (x - d / 2) / d;
		int _x = (x - d / 2) % d;

		int ny = (y - d / 2) / d;
		int _y = (y - d / 2) % d;

		if (nx >= ww - 1)
			return 1;

		if (ny >= hh - 1)
			return 1;

		double f00 = data[nx][ny];
		double f01 = data[nx][ny + 1];

		double f10 = data[nx + 1][ny];
		double f11 = data[nx + 1][ny + 1];

		return interpolate((double) _x / d, (double) _y / d, f00, f01, f10, f11);
	}

	double getExpectedBrightness(int xx, int yy) {
		double br0 = data[0][yy];
		double br1 = data[ww - 1][yy];

		double br2 = data[xx][0];
		double br3 = data[xx][hh - 1];

		double br_expected;
		if (horiz && !vert)
			br_expected = interpolate(0, ww, xx, br0, br1);
		else if (!horiz && vert)
			br_expected = interpolate(0, hh, yy, br2, br3);
		else
			br_expected = (interpolate(0, ww, xx, br0, br1) + interpolate(0,
					hh, yy, br2, br3)) / 2;

		return br_expected;
	}

	private double getAverBrightness(int x, int y, int d, double brExp) {
		double br = 0;
		int cnt = 0;
		for (int dy = 0; dy < d; dy++)
			for (int dx = 0; dx < d; dx++) {
				double b = doc.getBrightness(x + dx, y + dy);
				if (brExp == -1 || Math.abs(b - brExp) < brExp / 4) {
					br += b;
					cnt++;
				}
			}
		if (cnt < d)
			return brExp;
		return br / cnt;
	}
	double getAverBr(int xx, int yy, double brExp) {
		return getAverBrightness(xx * d, yy * d, d, brExp);
	}

	void run() {
		for (int yy = 0; yy < hh; yy++) {
			data[0][yy] = getAverBr(0, yy, -1);
			data[ww - 1][yy] = getAverBr(ww - 1, yy, -1);
		}

		for (int xx = 0; xx < ww; xx++) {
			data[xx][0] = getAverBr(xx, 0, -1);
			data[xx][hh - 1] = getAverBr(xx, hh - 1, -1);
		}

		for (int yy = 1; yy < hh - 1; yy++) {
			for (int xx = 1; xx < ww - 1; xx++) {
				double br_expected = getExpectedBrightness(xx, yy);
				double br = getAverBr(xx, yy, br_expected);
				if (br < br_expected && br > br_expected * 0.75)
					data[xx][yy] = br_expected / br;
				else
					data[xx][yy] = 1;
			}
		}

		for (int yy = 0; yy < hh; yy++) {
			data[0][yy] = 1;
			data[ww - 1][yy] = 1;
		}

		for (int xx = 0; xx < ww; xx++) {
			data[xx][0] = 1;
			data[xx][hh - 1] = 1;
		}

		for (int y = 0; y < spotH; y++)
			for (int x = 0; x < spotW; x++) {
				doc.adjustBrightness(x, y, getGain(x, y));
			}
	}

	public static void remove(Doc doc, int diam,
			boolean horiz, boolean vert) {
		int w = doc.getWidth();
		int h = doc.getHeight();
		if (w < 10 || h < 10 || diam < 2 || diam > w / 4 || diam > h / 4)
			return;

		SpotRem r = new SpotRem(doc, diam, horiz, vert);
		r.run();
	}

}
