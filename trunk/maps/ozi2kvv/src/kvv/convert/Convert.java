package kvv.convert;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import kvv.img.Img;
import kvv.img.Img.SrcImg;
import kvv.img.Img.Transformation;
import kvv.kvvmap.conversion.MatrixException;
import kvv.quantizer.ImageQuantizer;

public class Convert {

	static boolean debug = false;
	static int qual = -1;
	static boolean noborder = false;

	public static void main(String[] args) throws IOException, MatrixException {
		int zoom = 0;
		boolean yandex = false;
		Integer min = null;
		Integer max = null;
		boolean noAddPoints = false;
		int bpp = 6;
		String outDir = ".";

		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-zoom")) {
				zoom = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-min")) {
				min = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-max")) {
				max = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-qual")) {
				qual = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-bpp")) {
				bpp = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-out")) {
				outDir = args[i + 1];
				i++;
			} else if (args[i].equals("-debug")) {
				debug = true;
			} else if (args[i].equals("-yandex")) {
				yandex = true;
			} else if (args[i].equals("-noborder")) {
				noborder = true;
			} else if (args[i].equals("-noaddpoints")) {
				noAddPoints = true;
			} else {
				System.err.println("illegal parameter " + args[i]);
				System.exit(1);
			}

		}

		if (yandex) {
			File yandexDir = new File(args[0]);
			String[] zzs = yandexDir.list();
			for (String zs : zzs) {
				int z = Integer.parseInt(zs.substring(1));
				File zoomDir = new File(yandexDir, "z" + z);
				String[] yys = zoomDir.list();
				for (String ys : yys) {
					int ty = Integer.parseInt(ys);
					String[] xxs = new File(zoomDir, "" + ty).list();
					for (String xs : xxs) {
						try {
							int tx = Integer.parseInt(xs.substring(0,
									xs.lastIndexOf('.')));
							MapDescr1 mapDescr = new YandexTile(yandexDir, tx,
									ty, z);
							createTiles(mapDescr, z, 0, outDir);
						} catch (Exception e) {
						}
					}
				}
			}
		} else if (zoom != 0) {
			System.out.println(args[0]);
			long t = System.currentTimeMillis();

			File file = new File(args[0]);
			if (file.exists()) {
				MapDescr1 mapDescr = null;
				if (args[0].endsWith(".map"))
					mapDescr = new OziMapDescr(new File(args[0]), zoom, debug,
							min, max, noAddPoints, noborder);
				else if (args[0].endsWith(".cal"))
					mapDescr = new KvvMapDescr(new File(args[0]), zoom, debug,
							min, max, noAddPoints);
				else {
					System.err.println("unsupported map file type");
					System.exit(1);
				}
				createTiles(mapDescr, zoom, bpp, outDir);
				if (debug)
					System.out.println("time = "
							+ (System.currentTimeMillis() - t) / 1000 + " s");
			} else {
				System.out.println("file not found " + file);
			}
		} else {
			System.err.println("args: <mapFile> -zoom <zoom>");
			System.err.println("      or");
			System.err.println("      <yandexCacheDir> -yandex");
			System.exit(1);
		}
	}

	private static void createTiles(final MapDescr1 mapDescr, int zoom,
			int bpp, String outDir) throws IOException {

		File zoomDir = new File(outDir, "z" + zoom);
		zoomDir.mkdirs();

		double minDestX = mapDescr.getMinDestX();
		double maxDestX = mapDescr.getMaxDestX();
		double minDestY = mapDescr.getMinDestY();
		double maxDestY = mapDescr.getMaxDestY();
		double minSrcX = mapDescr.getSrcX((int) minDestX, (int) minDestY);
		double minSrcY = mapDescr.getSrcY((int) minDestX, (int) minDestY);
		double maxSrcX = mapDescr.getSrcX((int) maxDestX, (int) maxDestY);
		double maxSrcY = mapDescr.getSrcY((int) maxDestX, (int) maxDestY);

		double distDest = Math.sqrt((minDestX - maxDestX)
				* (minDestX - maxDestX) + (minDestY - maxDestY)
				* (minDestY - maxDestY));
		double distSrc = Math.sqrt((minSrcX - maxSrcX) * (minSrcX - maxSrcX)
				+ (minSrcY - maxSrcY) * (minSrcY - maxSrcY));

		final int factor = Math.max(1, (int) (distSrc / distDest));
		if (debug)
			System.out.println("factor = " + factor);

		Img.SrcImg src = new SrcImg() {
			@Override
			public int getRGB(int x, int y) {
				if (x < 0 || x >= mapDescr.getWidth() || y < 0
						|| y >= mapDescr.getHeight())
					return 0;

				int width = mapDescr.getWidth();
				int height = mapDescr.getHeight();

				int w = factor;

				int a = 0;
				int r = 0;
				int g = 0;
				int b = 0;

				int n = 0;

				for (int dx = 0; dx < w; dx++)
					for (int dy = 0; dy < w; dy++) {
						if (x + dx >= width || y + dy >= height)
							continue;
						int rgb = mapDescr.getRGB(x + dx, y + dy);
						a += rgb >>> 24;
						r += (rgb >>> 16) & 0xFF;
						g += (rgb >>> 8) & 0xFF;
						b += rgb & 0xFF;
						n++;
					}

				a /= n;
				r /= n;
				g /= n;
				b /= n;

				return (a << 24) + (r << 16) + (g << 8) + b;
			}
		};

		System.out.println();
		for (int y = mapDescr.getMinDestY() & 0xFFFFFF00; y < mapDescr
				.getMaxDestY(); y += 256) {
			final int _y = y;
			File ydir = new File(zoomDir, "" + (y >>> 8));
			ydir.mkdir();
			System.out.print((100 * (y - mapDescr.getMinDestY()) / (mapDescr
					.getMaxDestY() - mapDescr.getMinDestY())) + "%     \r");
			for (int x = mapDescr.getMinDestX() & 0xFFFFFF00; x < mapDescr
					.getMaxDestX(); x += 256) {
				final int _x = x;
				Img.Transformation trans = new Transformation() {
					@Override
					public long getSrcX(int dstX, int dstY) {
						return (long) (mapDescr.getSrcX(dstX + _x, dstY + _y) * (1L << 32));
					}

					@Override
					public long getSrcY(int dstX, int dstY) {
						return (long) (mapDescr.getSrcY(dstX + _x, dstY + _y) * (1L << 32));
					}
				};

				String[] names = { (x >>> 8) + ".png", (x >>> 8) + "_.png",
						(x >>> 8) + ".jpg", (x >>> 8) + "_.jpg",
						(x >>> 8) + ".gif", (x >>> 8) + "_.gif" };

				BufferedImage tile = null;

				for (String name : names) {
					try {
						File f = new File(ydir, name);
						tile = ImageIO.read(f);
						f.delete();
						break;
					} catch (Exception e) {
					}
				}
				if (tile == null)
					tile = new BufferedImage(256, 256,
							BufferedImage.TYPE_INT_ARGB);

				Img.DstImg dst = new Img.DstImgAdapter(tile);
				Img.transform(src, dst, trans);

				int transparent = 0;
				boolean hasPixels = false;
				for (int yy = 0; yy < tile.getHeight(); yy++) {
					for (int xx = 0; xx < tile.getWidth(); xx++) {
						if (tile.getRGB(xx, yy) == 0)
							transparent++;
						else
							hasPixels = true;
					}
				}

				if (hasPixels) {
					if (qual >= 0 && transparent < 500) {
						BufferedImage im = new BufferedImage(256, 256,
								BufferedImage.TYPE_INT_RGB);
						Graphics g = im.getGraphics();
						g.drawImage(tile, 0, 0, new ImageObserver() {
							@Override
							public boolean imageUpdate(Image img, int infoflags, int x, int y,
									int width, int height) {
								return (infoflags & ALLBITS) != 0;
							}
						});
						g.dispose();

						// ImageIO.write(im, "jpeg", new File(ydir, (x >>> 8)
						// + ".jpg"));

						Iterator<ImageWriter> iter = ImageIO
								.getImageWritersByFormatName("jpeg");
						ImageWriter writer = iter.next();
						ImageWriteParam iwp = writer.getDefaultWriteParam();
						iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
						iwp.setCompressionQuality(qual / 100f);
						File file = new File(ydir, (x >>> 8) + ".jpg");
						FileImageOutputStream output = new FileImageOutputStream(
								file);
						writer.setOutput(output);
						IIOImage image = new IIOImage(im, null, null);
						writer.write(null, image, iwp);
						writer.dispose();
					} else if (transparent < 1500) {
						if (bpp != 0)
							tile = ImageQuantizer.quantize(tile, bpp);

						ImageIO.write(tile, "png", new File(ydir, (x >>> 8)
								+ ".png"));
					} else {
						if (bpp != 0)
							tile = ImageQuantizer.quantize(tile, bpp);

						ImageIO.write(tile, "png", new File(ydir, (x >>> 8)
								+ "_.png"));
					}

					// if (transparent > 1500)
					// ImageIO.write(tile, "png", new File(ydir, (x >>> 8)
					// + "_.png"));
					// else
					// ImageIO.write(tile, "png", new File(ydir, (x >>> 8)
					// + ".png"));
				}

				tile.flush();
			}
		}
		System.out.println("               ");
	}

}
