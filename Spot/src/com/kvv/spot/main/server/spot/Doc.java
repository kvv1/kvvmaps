package com.kvv.spot.main.server.spot;

public interface Doc {

	int getWidth();

	int getHeight();

	double getBrightness(int x, int y);

	int getPixel(int x, int y);

	void setPixel(int x, int y, int pixel);

	void adjustBrightness(int x, int y, double gain);

}
