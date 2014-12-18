package kvv.mks.cloud;

import java.io.Serializable;

import kvv.mks.Util;
import kvv.mks.opt.TargetFunc;

public class FuncGridArray implements TargetFunc, Serializable{

	private final double step;
	double minx1;
	double miny1;
	double minz1;

	private double[][][] values;

	private double get(int x, int y, int z) {
		if(x < 0 || x >= values.length)
			return 0;
		if(y < 0 || y >= values[0].length)
			return 0;
		if(z < 0 || z >= values[0][0].length)
			return 0;
		return values[x][y][z];
	}

	private void set(int x, int y, int z, double val) {
		values[x][y][z] = val;
	}

	public FuncGridArray(TargetFunc func, double minX, double maxX,
			double minY, double maxY, double minZ, double maxZ, double step) {
		this.step = step;

		minx1 = Math.floor(minX / step) * step;
		miny1 = Math.floor(minY / step) * step;
		minz1 = Math.floor(minZ / step) * step;
		double maxx1 = Math.floor(maxX / step) * step;
		double maxy1 = Math.floor(maxY / step) * step;
		double maxz1 = Math.floor(maxZ / step) * step;

		int NX = (int) Math.floor((maxx1 - minx1) / step) + 2;
		int NY = (int) Math.floor((maxy1 - miny1) / step) + 2;
		int NZ = (int) Math.floor((maxz1 - minz1) / step) + 2;

		values = new double[NX][NY][NZ];

		int n = 0;

		for (int nx = 0; nx < NX; nx++)
			for (int ny = 0; ny < NY; ny++)
				for (int nz = 0; nz < NZ; nz++) {
					double x1 = minx1 + nx * step;
					double y1 = miny1 + ny * step;
					double z1 = minz1 + nz * step;
					double val = func.getValue(x1, y1, z1);
					if (val != 0)
						n++;
					set(nx, ny, nz, val / step / step / step);
				}

		System.out.println("N = " + n);
	}

	@Override
	public double getValue(double x, double y, double z) {

		int nx = (int) Math.floor((x - minx1) / step);
		int ny = (int) Math.floor((y - miny1) / step);
		int nz = (int) Math.floor((z - minz1) / step);

		double dx1 = x - (minx1 + nx * step);
		double dx2 = step - dx1;
		double dy1 = y - (miny1 + ny * step);
		double dy2 = step - dy1;
		double dz1 = z - (minz1 + nz * step);
		double dz2 = step - dz1;

		double f111 = get(nx, ny, nz);
		double f112 = get(nx, ny, nz + 1);
		double f121 = get(nx, ny + 1, nz);
		double f122 = get(nx, ny + 1, nz + 1);
		double f211 = get(nx + 1, ny, nz);
		double f212 = get(nx + 1, ny, nz + 1);
		double f221 = get(nx + 1, ny + 1, nz);
		double f222 = get(nx + 1, ny + 1, nz + 1);

		return f111 * dx2 * dy2 * dz2 + f112 * dx2 * dy2 * dz1 + f121 * dx2
				* dy1 * dz2 + f122 * dx2 * dy1 * dz1 + f211 * dx1 * dy2 * dz2
				+ f212 * dx1 * dy2 * dz1 + f221 * dx1 * dy1 * dz2 + f222 * dx1
				* dy1 * dz1;
	}

	public static void main(String[] args) {
		TargetFunc func = new TargetFunc() {
			@Override
			public double getValue(double x, double y, double z) {
				return 10 - Util.aver2(Math.abs(x), Math.abs(y), Math.abs(z));
			}
		};

		TargetFunc f2 = new FuncGridArray(func, -10, 10, -10, 10, -10, 10, 1);

		for (int i = 0; i < 100; i++) {
			double x = Util.rand(-10, 10);
			double y = Util.rand(-10, 10);
			double z = Util.rand(-10, 10);
			double v1 = func.getValue(x, y, z);
			double v2 = f2.getValue(x, y, z);
			System.out.printf("%7.3f %7.3f %7.3f %7.3f %7.3f %7.3f\n", x, y, z, v1,
					v2, Math.abs(v1 - v2));
		}

	}
}
