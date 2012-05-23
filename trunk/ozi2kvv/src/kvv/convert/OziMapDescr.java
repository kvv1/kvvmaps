package kvv.convert;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import kvv.kvvmap.conversion.MatrixException;

public class OziMapDescr extends MapDescrBase {

	public OziMapDescr(File inFile, int zoom, boolean debug, Integer min,
			Integer max, boolean noAddPoints, boolean noborder)
			throws IOException, MatrixException {

		boolean transverse = false;
		boolean pulkovo = false;
		ArrayList<Double> xarr = new ArrayList<Double>();
		ArrayList<Double> yarr = new ArrayList<Double>();
		ArrayList<Double> lonarr = new ArrayList<Double>();
		ArrayList<Double> latarr = new ArrayList<Double>();
		ArrayList<Point> mmpxy = new ArrayList<Point>();
		ArrayList<Point2D> mmpll = new ArrayList<Point2D>();
		File parent = inFile.getParentFile();

		BufferedReader in = new BufferedReader(new FileReader(inFile));

		String fname = null;

		String line;
		int n = 0;
		while ((line = in.readLine()) != null) {
			try {
				if (n == 2) {
					fname = line.trim();
					fname = new File(fname).getName();
				}
				if (n == 4) {
					if (line.startsWith("Pulkovo 1942")
							|| line.startsWith("S42"))
						pulkovo = true;
				}
				if (n == 8)
					transverse = line.contains("Transverse");

				String[] strings = line.split(",", -1);
				if (strings.length == 17 && strings[0].startsWith("Point")
						&& strings[1].trim().equals("xy")
						&& strings[2].trim().length() > 0
						&& !strings[4].contains("ex")) {
					int x = Integer.parseInt(strings[2].trim());
					int y = Integer.parseInt(strings[3].trim());
					double lat = Double.parseDouble(strings[6].trim());
					lat += Double.parseDouble(strings[7].trim()) / 60;
					double lon = Double.parseDouble(strings[9].trim());
					lon += Double.parseDouble(strings[10].trim()) / 60;

					xarr.add((double) x);
					yarr.add((double) y);
					lonarr.add(lon);
					latarr.add(lat);
				} else if (strings[0].trim().equals("MMPLL")) {
					double lon = Double.parseDouble(strings[2].trim());
					double lat = Double.parseDouble(strings[3].trim());
					int mmpn = Integer.parseInt(strings[1].trim());
					while (mmpll.size() < mmpn)
						mmpll.add(null);
					mmpll.set(mmpn - 1, new Point2D.Double(lon, lat));
				} else if (strings[0].trim().equals("MMPXY")) {
					int x = Integer.parseInt(strings[2].trim());
					int y = Integer.parseInt(strings[3].trim());
					int mmpn = Integer.parseInt(strings[1].trim());
					while (mmpxy.size() < mmpn)
						mmpxy.add(null);
					mmpxy.set(mmpn - 1, new Point(x, y));
				}
			} catch (NumberFormatException e) {
			}
			n++;
		}

		in.close();

		if (fname == null)
			throw new IOException("cannot parse .map file : no map name");

		if (noborder) {
			Point xyTL = new Point();
			Point xyTR = new Point();
			Point xyBR = new Point();
			Point xyBL = new Point();

			Point2D.Double llTL = new Point2D.Double(1000, -1000);
			Point2D.Double llTR = new Point2D.Double(-1000, -1000);
			Point2D.Double llBR = new Point2D.Double(-1000, 1000);
			Point2D.Double llBL = new Point2D.Double(1000, 1000);

			for (int i = 0; i < xarr.size(); i++) {
				double lon = lonarr.get(i);
				double lat = latarr.get(i);
				double x = xarr.get(i);
				double y = yarr.get(i);

				if (lon + lat > llTR.x + llTR.y) {
					llTR.setLocation(lon, lat);
					xyTR.setLocation(x, y);
				}

				if (lon + lat < llBL.x + llBL.y) {
					llBL.setLocation(lon, lat);
					xyBL.setLocation(x, y);
				}

				if (lon - lat > llBR.x - llBR.y) {
					llBR.setLocation(lon, lat);
					xyBR.setLocation(x, y);
				}

				if (lon - lat < llTL.x - llTL.y) {
					llTL.setLocation(lon, lat);
					xyTL.setLocation(x, y);
				}
			}

			mmpxy = new ArrayList<Point>();
			mmpll = new ArrayList<Point2D>();

			mmpxy.add(xyTL);
			mmpxy.add(xyTR);
			mmpxy.add(xyBR);
			mmpxy.add(xyBL);

			mmpll.add(llTL);
			mmpll.add(llTR);
			mmpll.add(llBR);
			mmpll.add(llBL);

			// System.out.println(xyTL + " " + llTL);
			// System.exit(0);
		}

		init(parent, fname, debug, min, max, noAddPoints, zoom, transverse,
				pulkovo, xarr, yarr, lonarr, latarr, mmpxy, mmpll);
	}
}
