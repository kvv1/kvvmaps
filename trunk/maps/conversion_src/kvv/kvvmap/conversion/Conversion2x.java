package kvv.kvvmap.conversion;

import Jama.Matrix;

public class Conversion2x implements Conversion{

	private Matrix a;
	private Matrix b;

	public Conversion2x(double[] x1, double[] y1, double[] x2, double[] y2)
			throws MatrixException {
		double[][] m = new double[6][6];
		double[] bForX = new double[6];
		double[] bForY = new double[6];
		for (int i = 0; i < x1.length; i++) {
			m[0][0]++;
			m[0][1] = m[1][0] += x1[i];
			m[0][2] = m[2][0] += y1[i];
			m[0][3] = m[3][0] = m[1][1] += Math.pow(x1[i], 2);
			m[0][4] = m[4][0] = m[1][2] = m[2][1] += x1[i] * y1[i];
			m[0][5] = m[5][0] = m[2][2] += Math.pow(y1[i], 2);
			m[1][3] = m[3][1] += Math.pow(x1[i], 3);
			m[1][4] = m[4][1] = m[2][3] = m[3][2] += Math.pow(x1[i], 2) * y1[i];
			m[1][5] = m[5][1] = m[2][4] = m[4][2] += x1[i] * Math.pow(y1[i], 2);
			m[2][5] = m[5][2] += Math.pow(y1[i], 3);
			m[3][3] += Math.pow(x1[i], 4);
			m[3][4] = m[4][3] += Math.pow(x1[i], 3) * y1[i];
			m[3][5] = m[5][3] = m[4][4] += Math.pow(x1[i], 2)
					* Math.pow(y1[i], 2);
			m[4][5] = m[5][4] += x1[i] * Math.pow(y1[i], 3);
			m[5][5] += Math.pow(y1[i], 4);

			bForX[0] += x2[i];
			bForX[1] += x1[i] * x2[i];
			bForX[2] += y1[i] * x2[i];
			bForX[3] += Math.pow(x1[i], 2) * x2[i];
			bForX[4] += x1[i] * y1[i] * x2[i];
			bForX[5] += Math.pow(y1[i], 2) * x2[i];

			bForY[0] += y2[i];
			bForY[1] += x1[i] * y2[i];
			bForY[2] += y1[i] * y2[i];
			bForY[3] += Math.pow(x1[i], 2) * y2[i];
			bForY[4] += x1[i] * y1[i] * y2[i];
			bForY[5] += Math.pow(y1[i], 2) * y2[i];
		}

		//org.ujmp.core.Matrix mm = new org.ujmp.core.Matrix();
		
		Matrix matrix = new Matrix(m);

		try {
			a = matrix.solve(new Matrix(bForX, 6));
			b = matrix.solve(new Matrix(bForY, 6));
		} catch (Throwable e) {
			throw new MatrixException(e);
		}
	}

	@Override
	public double getX(double x, double y) {
		return a.get(0, 0) + a.get(1, 0) * x + a.get(2, 0) * y + a.get(3, 0)
				* Math.pow(x, 2) + a.get(4, 0) * x * y + a.get(5, 0)
				* Math.pow(y, 2);
	}

	@Override
	public double getY(double x, double y) {
		return b.get(0, 0) + b.get(1, 0) * x + b.get(2, 0) * y + b.get(3, 0)
				* Math.pow(x, 2) + b.get(4, 0) * x * y + b.get(5, 0)
				* Math.pow(y, 2);
	}
}
