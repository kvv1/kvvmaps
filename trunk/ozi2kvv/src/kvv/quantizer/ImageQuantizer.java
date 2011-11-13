package kvv.quantizer;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class ImageQuantizer {
	public static BufferedImage quantize(BufferedImage img, int bpp) {
		int colors = 1 << bpp;

		int pixels[][] = getPixels(img);
		int palette[] = Arrays.copyOf(
				Quantize.quantizeImage(pixels, colors - 1), colors - 1);
		BufferedImage i_img = new BufferedImage(img.getWidth(),
				img.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);

		ColorModel cm = i_img.getColorModel();
		IndexColorModel icm = (IndexColorModel) cm;
		WritableRaster raster = i_img.getRaster();

		int size = icm.getMapSize();

		byte[] reds = new byte[size];
		byte[] greens = new byte[size];
		byte[] blues = new byte[size];

		for (int i = 0; i < colors - 1; i++) {
			reds[i] = (byte) (palette[i] >> 16);
			greens[i] = (byte) (palette[i] >> 8);
			blues[i] = (byte) palette[i];
		}

		IndexColorModel icm2 = new IndexColorModel(8, size, reds, greens,
				blues, colors - 1);

		int[] pix = new int[1];

		for (int y = 0; y < img.getHeight(); y++)
			for (int x = 0; x < img.getWidth(); x++) {
				if (img.getRGB(x, y) >>> 24 > 128) {
					pix[0] = pixels[x][y];
				} else {
					pix[0] = colors - 1;
				}
				raster.setPixel(x, y, pix);
			}

		BufferedImage dest = new BufferedImage(icm2, raster,
				i_img.isAlphaPremultiplied(), null);

		return dest;
	}

	private static int[][] getPixels(Image image) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		int pix[] = new int[w * h];
		PixelGrabber grabber = new PixelGrabber(image, 0, 0, w, h, pix, 0, w);

		try {
			if (grabber.grabPixels() != true)
				return null;
			int pixels[][] = new int[w][h];
			for (int x = w; x-- > 0;) {
				for (int y = h; y-- > 0;) {
					pixels[x][y] = pix[y * w + x];
				}
			}
			return pixels;
		} catch (InterruptedException e) {
			return null;
		}
	}
}
