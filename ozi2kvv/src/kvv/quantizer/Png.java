package kvv.quantizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Png {

	public static void main(String[] args) throws IOException {
		BufferedImage img = null;
		// img = ImageIO.read(new File("610.gif"));
		img = ImageIO.read(new File("606.gif"));
		// img = ImageIO.read(new File("a.png"));

		BufferedImage dest = ImageQuantizer.quantize(img, 5);

		ImageIO.write(dest, "gif", new File("b.gif"));
	}
}
