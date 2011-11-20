package com.kvv.spot.main.server.spot;

public abstract class DocImpl implements Doc {

	private static int _getR(int pixel) {
		return pixel & 0xFF;
	}

	private static int _getG(int pixel) {
		return (pixel >> 8) & 0xFF;
	}

	private static int _getB(int pixel) {
		return (pixel >> 16) & 0xFF;
	}

	private static int _makePixel(int r, int g, int b) {
		return r + (g << 8) + (b << 16);
	}

	public final void adjustBrightness(int x, int y, double gain) {
		int pixel = getPixel(x, y);

		int r = _getR(pixel);
		int g = _getG(pixel);
		int b = _getB(pixel);

		r *= gain;
		g *= gain;
		b *= gain;

		if (r > 255)
			r = 255;
		if (g > 255)
			g = 255;
		if (b > 255)
			b = 255;

		pixel = _makePixel(r, g, b);
		setPixel(x, y, pixel);
	}

	public final double getBrightness(int x, int y) {
		int pixel = 0;

		try {
			pixel = getPixel(x,y);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		int r = _getR(pixel);
		int g = _getG(pixel);
		int b = _getB(pixel);

		return (r + g + b) / 3;
	}
}
