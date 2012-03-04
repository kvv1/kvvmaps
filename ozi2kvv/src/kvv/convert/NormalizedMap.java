package kvv.convert;

public abstract class NormalizedMap implements MapDescr {

	private final MapDescr map;
	private final boolean debug;
	private int min;
	private int max;

	protected abstract boolean hitTest(int x, int y);

	public NormalizedMap(MapDescr map, boolean debug, Integer min, Integer max) {
		this.map = map;
		this.debug = debug;

		calcBrightness(min, max);
	}

	@Override
	public int getRGB(int x, int y) {
		if (!hitTest(x, y))
			return 0;
		return normalize(map.getRGB(x, y));
	}

	@Override
	public int getWidth() {
		return map.getWidth();
	}

	@Override
	public int getHeight() {
		return map.getHeight();
	}

	private void calcBrightness(Integer min, Integer max) {
		if (min != null)
			this.min = min;

		if (max != null)
			this.max = max;

		if (min != null && max != null)
			return;

		long[] histo = new long[256];
		long cnt = getHisto(histo);

		if (debug) {
			for (int i = 0; i < histo.length; i++) {
				long n = histo[i];
				System.out.println(i + "\t" + n * 100 / cnt + "\t" + n);
			}
		}

		if (min == null) {
			long sum = 0;
			for (int i = 0; i < histo.length; i++) {
				sum += histo[i];
				if (sum * 1000 / cnt > 10) {
					this.min = i;
					break;
				}
			}
		}

		if (max == null) {
			long sum = 0;
			for (int i = histo.length - 1; i >= 0; i--) {
				sum += histo[i];
				if (sum * 100 / cnt > 4) {
					this.max = i;
					break;
				}
			}
		}

		if (debug)
			System.out.println("min = " + this.min + " max = " + this.max);
	}

	private int normalizeBr(int n) {
		// if(n < mean)
		// n = 127 * (n - min) / (mean - min);
		// else
		// n = 128 + 127 * (n - mean) / (max - mean);
		n = 255 * (n - min) / Math.max(10, max - min);

		if (n < 0)
			n = 0;
		if (n > 255)
			n = 255;

		return n;
	}

	private int normalize(int pix) {
		int r = pix & 0xFF;
		int g = (pix >> 8) & 0xFF;
		int b = (pix >> 16) & 0xFF;

		r = normalizeBr(r);
		g = normalizeBr(g);
		b = normalizeBr(b);

		return (pix & 0xFF000000) | r | (g << 8) | (b << 16);
	}

	private long getHisto(long[] histo) {
		long cnt = 0;
		int h = map.getHeight();
		int w = map.getWidth();

		for (int yy = 0; yy < h; yy += 64) {
			System.out.print("histo " + (yy * 100 / h) + "%     \r");
			int maxy = Math.min(yy + 64, h);
			for (int xx = 0; xx < w; xx += 64) {
				int maxx = Math.min(xx + 64, w);
				for (int y = yy; y < maxy; y++) {
					for (int x = xx; x < maxx; x++) {
						if (!hitTest(x, y))
							continue;
						cnt++;
						int pix = map.getRGB(x, y);
						int br = (pix & 0xFF) + ((pix >> 8) & 0xFF)
								+ ((pix >> 16) & 0xFF);
						br /= 3;
						histo[br]++;
					}
				}
			}
		}

		System.out.println("               ");
		return cnt;
	}

}
