package kvv.convert;

import kvv.kvvmap.conversion.Conversion2x;
import kvv.kvvmap.conversion.MatrixException;

public class Pulkovo1942 {
	private static final double[] lat1 = {70, 		70,			70,			50, 		50,			50,			30, 		30,			30};
	private static final double[] lon1 = {30, 		100,		170,		30, 		100,		170,		30, 		100,		170};
	private static final double[] lat2 = {70.00010, 70.00099,	70.00027,	49.99982,	50.00056,	49.99998,	29.99953,	30.00005,	29.99970};
	private static final double[] lon2 = {29.99658, 99.99999,	170.00372,	29.99830,	100.00013,	170.00209,	29.99881,	100.00018,	170.00162};
	
	private static Conversion2x datumConv;
	
	static {
		try {
			datumConv = new Conversion2x(lon1, lat1, lon2, lat2);
		} catch (MatrixException e1) {
			e1.printStackTrace();
		}
	}
	
	public static double getLon(double lon, double lat) {
		return datumConv.getX(lon, lat);
	}

	public static double getLat(double lon, double lat) {
		return datumConv.getY(lon, lat);
	}
}
