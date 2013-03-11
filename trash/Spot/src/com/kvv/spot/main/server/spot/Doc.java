package com.kvv.spot.main.server.spot;

public interface Doc {

	int getWidth();

	int getHeight();

	int getPixel(int x, int y);

	void setPixel(int x, int y, int pixel);

	int getR(int pixel);

	int getG(int pixel);

	int getB(int pixel);

	int makePixel(int r, int g, int b);
}
