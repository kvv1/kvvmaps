package kvv.mks.cloud;

import java.util.HashMap;
import java.util.Map;

import kvv.mks.opt.TargetFunc;

public class FuncGridHash implements TargetFunc {

	static class Params {
		double f111;
		double f112;
		double f121;
		double f122;
		double f211;
		double f212;
		double f221;
		double f222;

		double x;
		double y;
		double z;
	}

	private final double step;

	private Map<Long, Params> map = new HashMap<>();

	public FuncGridHash(TargetFunc func, double minX, double maxX, double minY,
			double maxY, double minZ, double maxZ, double step) {
		this.step = step;

		double minx1 = Math.floor(minX / step) * step;
		double miny1 = Math.floor(minY / step) * step;
		double minz1 = Math.floor(minZ / step) * step;

		double maxx1 = Math.floor(maxX / step) * step;
		double maxy1 = Math.floor(maxY / step) * step;
		double maxz1 = Math.floor(maxZ / step) * step;

//		Map<Long, Double> map1 = new HashMap<>();
//
//		double[] values = 
//		
//		for (int nx = 0; nx < (maxx1 - minx1) / step; nx++)
//			for (int ny = 0; ny < (maxy1 - miny1) / step; ny++)
//				for (int nz = 0; nz < (maxz1 - minz1) / step; nz++) {
//					double x1 = minx1 + nx * step;
//					double y1 = miny1 + ny * step;
//					double z1 = minz1 + nz * step;
//					map1.put(hash(x1 + step/2, y1 + step/2, z1 + step/2), func.getValue(x1, y1, z1));
//				}

		for (double x1 = minx1; x1 <= maxx1; x1 += step) {
			System.out.println(x1);
			for (double y1 = miny1; y1 <= maxy1; y1 += step)
				for (double z1 = minz1; z1 <= maxz1; z1 += step) {
					double x2 = x1 + step;
					double y2 = y1 + step;
					double z2 = z1 + step;

					Params params = new Params();
					params.f111 = func.getValue(x1, y1, z1) / step / step
							/ step;
					params.f112 = func.getValue(x1, y1, z2) / step / step
							/ step;
					params.f121 = func.getValue(x1, y2, z1) / step / step
							/ step;
					params.f122 = func.getValue(x1, y2, z2) / step / step
							/ step;
					params.f211 = func.getValue(x2, y1, z1) / step / step
							/ step;
					params.f212 = func.getValue(x2, y1, z2) / step / step
							/ step;
					params.f221 = func.getValue(x2, y2, z1) / step / step
							/ step;
					params.f222 = func.getValue(x2, y2, z2) / step / step
							/ step;

					params.x = x1;
					params.y = y1;
					params.z = z1;

					if (params.f111 == 0 && params.f112 == 0
							&& params.f121 == 0 && params.f122 == 0
							&& params.f211 == 0 && params.f212 == 0
							&& params.f221 == 0 && params.f222 == 0)
						continue;

					map.put(hash(x1 + step / 2, y1 + step / 2, z1 + step / 2),
							params);

				}
		}
	}

	@Override
	public double getValue(double x, double y, double z) {
		Params params = map.get(hash(x, y, z));
		if (params == null)
			return 0;

		double dx1 = x - params.x;
		double dx2 = params.x + step - x;
		double dy1 = y - params.y;
		double dy2 = params.y + step - y;
		double dz1 = z - params.z;
		double dz2 = params.z + step - z;

		return params.f111 * dx2 * dy2 * dz2 + params.f112 * dx2 * dy2 * dz1
				+ params.f121 * dx2 * dy1 * dz2 + params.f122 * dx2 * dy1 * dz1
				+ params.f211 * dx1 * dy2 * dz2 + params.f212 * dx1 * dy2 * dz1
				+ params.f221 * dx1 * dy1 * dz2 + params.f222 * dx1 * dy1 * dz1;
	}

	private long hash(double x, double y, double z) {
		long x1 = (long) Math.floor(x / step);
		long y1 = (long) Math.floor(y / step);
		long z1 = (long) Math.floor(z / step);
		return hashInt(x1, y1, z1);
	}

	private long hashInt(long x, long y, long z) {
		if (x >> 20 != 0 && x >> 20 != -1)
			throw new IllegalArgumentException("" + x);
		if (y >> 20 != 0 && y >> 20 != -1)
			throw new IllegalArgumentException("" + y);
		if (z >> 20 != 0 && z >> 20 != -1)
			throw new IllegalArgumentException("" + z);

		long mask = (1 << 21) - 1;
		return (x & mask) + ((y & mask) << 21) + ((z & mask) << 42);
	}

}
