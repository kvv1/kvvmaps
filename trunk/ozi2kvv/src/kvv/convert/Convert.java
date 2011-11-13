package kvv.convert;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import kvv.img.Img;
import kvv.img.Img.SrcImg;
import kvv.img.Img.Transformation;
import kvv.kvvmap.conversion.MatrixException;
import kvv.quantizer.ImageQuantizer;

public class Convert {

	public static void main(String[] args) throws IOException, MatrixException {
		int zoom = 0;
		boolean resize = false;
		int scale = 0;
		boolean debug = false;
		boolean yandex = false;
		Integer min = null; 
		Integer max = null;

		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-zoom")) {
				zoom = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-scale")) {
				scale = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-min")) {
				min = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-max")) {
				max = Integer.parseInt(args[i + 1]);
				i++;
			} else if (args[i].equals("-resize")) {
				resize = true;
			} else if (args[i].equals("-debug")) {
				debug = true;
			} else if (args[i].equals("-yandex")) {
				yandex = true;
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
							createTiles(mapDescr, z, false, 0);
						} catch (Exception e) {
						}
					}
				}
			}
		} else if (zoom != 0) {
			System.out.println(args[0]);
			long t = System.currentTimeMillis();
			
			MapDescr1 mapDescr = new OziMapDescr(new File(args[0]), zoom,
					scale, debug, min, max);
			createTiles(mapDescr, zoom, resize, 5);
			if(debug)
				System.out.println("time = " + (System.currentTimeMillis() - t) / 1000 + " s");
		} else {
			System.err.println("args: <mapFile> -zoom <zoom>");
			System.err.println("      or");
			System.err.println("      <yandexCacheDir> -yandex");
			System.exit(1);
		}
	}

	private static void createTiles(final MapDescr1 mapDescr, int zoom,
			final boolean resize, int bpp) throws IOException {
		File zoomDir = new File("z" + zoom);
		zoomDir.mkdir();

		Img.SrcImg src = new SrcImg() {
			@Override
			public int getRGB(int x, int y) {
				if (x < 0 || x >= mapDescr.getWidth() || y < 0
						|| y >= mapDescr.getHeight())
					return 0;

				int rgb = mapDescr.getRGB(x, y);
				if (resize && y + 1 < mapDescr.getHeight()
						&& x + 1 < mapDescr.getWidth()) {
					int pix00 = rgb;
					int pix01 = mapDescr.getRGB(x, y + 1);
					int pix10 = mapDescr.getRGB(x + 1, y);
					int pix11 = mapDescr.getRGB(x + 1, y + 1);
					rgb = Img.merge(pix00, pix01, pix10, pix11, 128, 128);
				}

				return rgb;
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
					public long getX(int dstX, int dstY) {
						return (long) (mapDescr.getSrcX(dstX + _x, dstY + _y) * (1L << 32));
					}

					@Override
					public long getY(int dstX, int dstY) {
						return (long) (mapDescr.getSrcY(dstX + _x, dstY + _y) * (1L << 32));
					}
				};

				String[] names = { (x >>> 8) + ".png", (x >>> 8) + "_.png",
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
				for (int yy = 0; yy < dst.getHeight(); yy++) {
					for (int xx = 0; xx < dst.getWidth(); xx++) {
						if (tile.getRGB(xx, yy) == 0)
							transparent++;
						else
							hasPixels = true;
					}
				}

				if (bpp != 0)
					tile = ImageQuantizer.quantize(tile, bpp);

				if (hasPixels) {
					if (transparent > 1500)
						ImageIO.write(tile, "png", new File(ydir, (x >>> 8)
								+ "_.png"));
					else
						ImageIO.write(tile, "png", new File(ydir, (x >>> 8)
								+ ".png"));
				}

				tile.flush();
			}
		}
		System.out.println("               ");
	}
}
