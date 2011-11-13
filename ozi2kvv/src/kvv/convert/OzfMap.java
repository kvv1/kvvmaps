package kvv.convert;

import java.io.File;
import java.io.IOException;

import kvv.ozf.OzfFile;

public class OzfMap implements MapDescr {

	private OzfFile ozf;
	
	public OzfMap(File file) throws IOException {
		ozf = new OzfFile(file.getAbsolutePath(), 0);
	}

	@Override
	public int getRGB(int x, int y) {
		return ozf.getPixel(x, y);
	}

	@Override
	public int getWidth() {
		return ozf.getWidth();
	}

	@Override
	public int getHeight() {
		return ozf.getHeight();
	}
}
