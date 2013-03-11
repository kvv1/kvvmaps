package kvv.img;

import java.awt.image.BufferedImage;

public class Img {

	public interface SrcImg {
		int getRGB(int x, int y);
	}

	public interface DstImg {
		int getWidth();

		int getHeight();

		void setRGB(int x, int y, int rgb);
	}

	public static class DstImgAdapter implements DstImg {

		private final BufferedImage img;
		private boolean transparent = true;

		public DstImgAdapter(BufferedImage img) {
			this.img = img;
		}

		@Override
		public int getWidth() {
			return img.getWidth();
		}

		@Override
		public int getHeight() {
			return img.getHeight();
		}

		@Override
		public void setRGB(int x, int y, int rgb) {
			if (rgb != 0) {
				img.setRGB(x, y, rgb);
				transparent = false;
			}
		}

		public boolean isTransparent() {
			return transparent;
		}
	}

	public interface Transformation {
		long getSrcX(int dstX, int dstY);

		long getSrcY(int dstX, int dstY);
	}

	public static void transform(SrcImg src, DstImg dst, Transformation trans) {
		int w = dst.getWidth();
		int h = dst.getHeight();

		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				long srcX = trans.getSrcX(x, y);
				long srcY = trans.getSrcY(x, y);

				int pixel00 = src.getRGB((int) (srcX >>> 32),
						(int) (srcY >>> 32));
				int pixel01 = src.getRGB((int) (srcX >>> 32),
						(int) (srcY >>> 32) + 1);
				int pixel10 = src.getRGB((int) (srcX >>> 32) + 1,
						(int) (srcY >>> 32));
				int pixel11 = src.getRGB((int) (srcX >>> 32) + 1,
						(int) (srcY >>> 32) + 1);

				int pixel = merge(pixel00, pixel01, pixel10, pixel11,
						((int) srcX) >>> 24, ((int) srcY) >>> 24);

				dst.setRGB(x, y, pixel);
			}
	}

	public static int merge(int pixel00, int pixel01, int pixel10,
			int pixel11, int weightX, int weightY) {

		int w00 = ((256 - weightX) * (256 - weightY)) >>> 8;
		int w01 = ((256 - weightX) * weightY) >>> 8;
		int w10 = (weightX * (256 - weightY)) >>> 8;
		int w11 = (weightX * weightY) >>> 8;

		// int w = w00 + w01 + w10 + w11;
		// System.out.println(" " + w);

		return merge(pixel00, w00, pixel01, w01, pixel10, w10, pixel11, w11,
				0xFF)
				+ merge(pixel00, w00, pixel01, w01, pixel10, w10, pixel11, w11,
						0xFF00)
				+ merge(pixel00, w00, pixel01, w01, pixel10, w10, pixel11, w11,
						0xFF0000)
				+ merge(pixel00, w00, pixel01, w01, pixel10, w10, pixel11, w11,
						0xFF000000);
	}

	private static int merge(int pixel00, int w00, int pixel01, int w01,
			int pixel10, int w10, int pixel11, int w11, int mask) {

		return (int) (((pixel00 & mask) * (long) w00 + (pixel01 & mask)
				* (long) w01 + (pixel10 & mask) * (long) w10 + (pixel11 & mask)
				* (long) w11) >>> 8)
				& mask;
	}

}
