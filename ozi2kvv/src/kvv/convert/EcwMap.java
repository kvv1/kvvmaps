package kvv.convert;

import java.io.File;
import java.io.IOException;

import kvv.ecw.EcwFile;

public class EcwMap implements MapDescr {

	private EcwFile ecw;

	public EcwMap(File file) throws IOException {
		ecw = new EcwFile(file.getAbsolutePath(), 0);
	}

	@Override
	public int getRGB(int x, int y) {
		return ecw.getPixel(x, y);
	}

	@Override
	public int getWidth() {
		return ecw.getWidth();
	}

	@Override
	public int getHeight() {
		return ecw.getHeight();
	}
}
