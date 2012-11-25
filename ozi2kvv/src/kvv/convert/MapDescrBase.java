package kvv.convert;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import kvv.convert.Utils.PoligonBorder;
import kvv.kvvmap.conversion.Conversion2x;
import kvv.kvvmap.conversion.MatrixException;
import kvv.utils.Spline;

public class MapDescrBase implements MapDescr1 {

	private ArrayList<Double> xarr;
	private ArrayList<Double> yarr;
	private ArrayList<Double> lonarr;
	private ArrayList<Double> latarr;

	private ArrayList<Point> mmpxy;
	private ArrayList<Point2D> mmpll;
	private int zoom;
	private boolean transverse;
	private boolean pulkovo;

	private int[][] border = new int[2][];
	private Conversion2x conv2x;
	private MapDescr map;
	private double minNewX;
	private double maxNewX;
	private double minNewY;
	private double maxNewY;

	public MapDescrBase() {
	}

	protected void init(File parent, String fname, boolean debug, Integer min,
			Integer max, boolean noAddPoints, int zoom, boolean transverse,
			boolean pulkovo, ArrayList<Double> xarr, ArrayList<Double> yarr,
			ArrayList<Double> lonarr, ArrayList<Double> latarr,
			ArrayList<Point> mmpxy, ArrayList<Point2D> mmpll)
			throws IOException, MatrixException {
		this.zoom = zoom;
		this.transverse = transverse;
		this.pulkovo = pulkovo;
		this.xarr = xarr;
		this.yarr = yarr;
		this.lonarr = lonarr;
		this.latarr = latarr;
		this.mmpxy = mmpxy;
		this.mmpll = mmpll;

		if (debug)
			System.out.println("file '"
					+ new File(parent, fname).getAbsolutePath() + "'");

		String ext = fname.substring(fname.lastIndexOf('.') + 1).toLowerCase();

		MapDescr map1 = null;

		if (ext.equals("jpg") || ext.equals("png") || ext.equals("gif")
				|| ext.equals("bmp")) {
			map1 = new ImgMap(new File(parent, fname));
		} else if (ext.equals("ozf2") || ext.equals("ozfx3")) {
			map1 = new OzfMap(new File(parent, fname));
		} else if (ext.equals("ecw")) {
			map1 = new EcwMap(new File(parent, fname));
		}

		if (!noAddPoints)
			addBorderPoints();
		prepare();
		
		final PoligonBorder pb = new PoligonBorder(border[0], border[1]);

		map = new NormalizedMap(map1, debug, min, max) {
			@Override
			protected boolean hitTest(int x, int y) {
				return pb.test(x, y);
			}
		};

	}

	private void prepare() throws MatrixException, IOException {
		int sz = xarr.size() + this.mmpll.size();
		double[] xarr = new double[sz];
		double[] yarr = new double[sz];
		double[] newxarr = new double[sz];
		double[] newyarr = new double[sz];

		final int[] borderX = new int[this.mmpxy.size()];
		final int[] borderY = new int[this.mmpxy.size()];

		int n = 0;
		for (int i = 0; i < this.xarr.size(); i++) {
			xarr[n] = this.xarr.get(i);
			yarr[n] = this.yarr.get(i);

			double lon = this.lonarr.get(i);
			double lat = this.latarr.get(i);

			if (pulkovo) {
				double lon1 = Pulkovo1942.getLon(lon, lat);
				lat = Pulkovo1942.getLat(lon, lat);
				lon = lon1;
			}

			newxarr[n] = Utils.lon2x(lon, zoom);
			newyarr[n] = Utils.lat2y(lat, zoom);
			n++;
		}

		for (int i = 0; i < this.mmpxy.size(); i++) {
			Point xy = this.mmpxy.get(i);
			Point2D ll = this.mmpll.get(i);

			double lon = ll.getX();
			double lat = ll.getY();

			if (pulkovo) {
				double lon1 = Pulkovo1942.getLon(lon, lat);
				lat = Pulkovo1942.getLat(lon, lat);
				lon = lon1;
			}

			xarr[n] = xy.x;
			yarr[n] = xy.y;
			newxarr[n] = Utils.lon2x(lon, zoom);
			newyarr[n] = Utils.lat2y(lat, zoom);
			borderX[i] = xy.x;
			borderY[i] = xy.y;
			n++;
		}

		border[0] = borderX;
		border[1] = borderY;

		conv2x = new Conversion2x(newxarr, newyarr, xarr, yarr);

		Arrays.sort(newxarr);
		Arrays.sort(newyarr);

		minNewX = newxarr[0];
		maxNewX = newxarr[newxarr.length - 1];
		minNewY = newyarr[0];
		maxNewY = newyarr[newyarr.length - 1];
	}

	private void addBorderPoints() {
		int sz = mmpxy.size();

		if (sz < 6) {

			ArrayList<Point> _mmpxy = new ArrayList<Point>();
			ArrayList<Point2D> _mmpll = new ArrayList<Point2D>();

			for (int i = 0; i < sz; i++) {
				int j = (i + 1) % sz;

				int x1 = mmpxy.get(i).x;
				int x2 = mmpxy.get(j).x;

				int y1 = mmpxy.get(i).y;
				int y2 = mmpxy.get(j).y;

				double lon1 = mmpll.get(i).getX();
				double lon2 = mmpll.get(j).getX();

				double lat1 = mmpll.get(i).getY();
				double lat2 = mmpll.get(j).getY();

				int xc = (x1 + x2) / 2;
				int yc;

				double lonc = (lon1 + lon2) / 2;
				double latc = (lat1 + lat2) / 2;

				if (transverse) {
					int dy = getDY(x2 - x1, latc, lon1, lon2, lonc);
					yc = (y1 + y2) / 2 + dy;
				} else {
					if (Math.abs(lat2 - lat1) > 0.0001)
						yc = (int) (y1 + (y2 - y1)
								* (Utils.lat2y(latc, 15) - Utils
										.lat2y(lat1, 15))
								/ (Utils.lat2y(lat2, 15) - Utils
										.lat2y(lat1, 15)));
					else
						yc = (y1 + y2) / 2;
				}

				_mmpxy.add(mmpxy.get(i));
				_mmpll.add(mmpll.get(i));

				_mmpxy.add(new Point(xc, yc));
				_mmpll.add(new Point2D.Double(lonc, latc));
			}

			mmpxy = _mmpxy;
			mmpll = _mmpll;

		}
		if (transverse) {
			ArrayList<Point> _mmpxy = new ArrayList<Point>();
			ArrayList<Point2D> _mmpll = new ArrayList<Point2D>();

			sz = mmpxy.size();

			for (int i = 0; i < sz; i++) {
				int j = (i + 1) % sz;

				int x1 = mmpxy.get(i).x;
				int x2 = mmpxy.get(j).x;

				int y1 = mmpxy.get(i).y;
				int y2 = mmpxy.get(j).y;

				double lon1 = mmpll.get(i).getX();
				double lon2 = mmpll.get(j).getX();

				double lat1 = mmpll.get(i).getY();
				double lat2 = mmpll.get(j).getY();

				_mmpxy.add(mmpxy.get(i));
				_mmpll.add(mmpll.get(i));

				int n = (int) Math.abs(lon2 - lon1) * 2;

				for (int k = 1; k < n; k++) {
					double lonc = lon1 + k * (lon2 - lon1) / n;
					double latc = lat1 + k * (lat2 - lat1) / n;
					int xc = x1 + k * (x2 - x1) / n;
					int yc = y1 + k * (y2 - y1) / n;

					int dy = getDY(x2 - x1, latc, lon1, lon2, lonc);

					_mmpxy.add(new Point(xc, yc + dy));
					_mmpll.add(new Point2D.Double(lonc, latc));
				}
			}

			mmpxy = _mmpxy;
			mmpll = _mmpll;
		}
	}

	private final static Spline spline = new Spline(new double[] { 0, 34, 62,
			74, 84 }, new double[] { 0, 11, 20, 20, 22.5 });

	private static int getDY(int dx, double lat, double lon1, double lon2,
			double lon) {

		double dlon = lon2 - lon1;
		double lonc = (lon1 + lon2) / 2;
		double dl = lon - lonc;

		double k = spline.splineValue(Math.abs(lat));

		double dy = k * Math.abs(dx) * (dlon * dlon / 4 - dl * dl)
				/ Math.abs(dlon);

		if (lat < 0)
			dy = -dy;

		int res = (int) dy / 2511;
		
		return res;
	}

	@Override
	public int getMinDestY() {
		return (int) minNewY;
	}

	@Override
	public int getMinDestX() {
		return (int) minNewX;
	}

	@Override
	public int getMaxDestY() {
		return (int) maxNewY;
	}

	@Override
	public int getMaxDestX() {
		return (int) maxNewX;
	}

	@Override
	public double getSrcX(int dstX, int dstY) {
		double x = conv2x.getX(dstX, dstY);
		return x;
	}

	@Override
	public double getSrcY(int dstX, int dstY) {
		double y = conv2x.getY(dstX, dstY);
		return y;
	}

	@Override
	public int getRGB(int x, int y) {
		return map.getRGB(x, y);
	}

	@Override
	public int getWidth() {
		return map.getWidth();
	}

	@Override
	public int getHeight() {
		return map.getHeight();
	}
}
