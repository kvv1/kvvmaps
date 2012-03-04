package kvv.convert;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import kvv.kvvmap.conversion.MatrixException;

public class KvvMapDescr extends MapDescrBase {

	private ArrayList<Double> xarr = new ArrayList<Double>();
	private ArrayList<Double> yarr = new ArrayList<Double>();
	private ArrayList<Double> lonarr = new ArrayList<Double>();
	private ArrayList<Double> latarr = new ArrayList<Double>();
	private ArrayList<Point> mmpxy = new ArrayList<Point>();
	private ArrayList<Point2D> mmpll = new ArrayList<Point2D>();

	public KvvMapDescr(File inFile, int zoom, boolean debug, Integer min,
			Integer max, boolean noAddPoints) throws IOException,
			MatrixException {

		boolean transverse = true;
		boolean pulkovo = true;

		File parent = inFile.getParentFile();

		Properties props = new Properties();
		props.load(new FileInputStream(inFile));

		String fname = props.getProperty("img");

		addPoint(props.getProperty("point1"));
		addPoint(props.getProperty("point2"));
		addPoint(props.getProperty("point3"));
		addPoint(props.getProperty("point4"));

		init(parent, fname, debug, min, max, noAddPoints, zoom, transverse,
				pulkovo, xarr, yarr, lonarr, latarr, mmpxy, mmpll);
	}

	private void addPoint(String calPt) {
		String[] ss = calPt.split(",");
		double x = Integer.parseInt(ss[0]);
		double y = Integer.parseInt(ss[1]);
		double lon = Double.parseDouble(ss[2]);
		double lat = Double.parseDouble(ss[3]);

		xarr.add(x);
		yarr.add(y);
		lonarr.add(lon);
		latarr.add(lat);

		mmpxy.add(new Point((int) x, (int) y));
		mmpll.add(new Point2D.Double(lon, lat));
	}
}
