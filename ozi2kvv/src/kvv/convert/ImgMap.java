package kvv.convert;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImgMap implements MapDescr {

	private BufferedImage img;
	
	public ImgMap(File file) throws IOException {
		System.out.println("img file '" + file.getAbsolutePath()
				+ "'");
		img = ImageIO.read(file);
	}

	@Override
	public int getRGB(int x, int y) {
		return img.getRGB(x, y);
	}

	@Override
	public int getWidth() {
		return img.getWidth();
	}

	@Override
	public int getHeight() {
		return img.getHeight();
	}
}
