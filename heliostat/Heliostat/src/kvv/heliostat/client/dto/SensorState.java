package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SensorState implements Serializable {
	public int tl;
	public int tr;
	public int bl;
	public int br;

	public SensorState() {
	}

	public SensorState(int tl, int tr, int bl, int br) {
		this.tl = tl;
		this.tr = tr;
		this.bl = bl;
		this.br = br;
	}

	public boolean isValid() {
		return tr + tl + br + bl > 100;
	}

	public double getDeflectionX() {
		double sum = tr + tl + br + bl;
		if (sum == 0)
			return 0;

		double x = tr - tl + br - bl;
		return x / sum * 4;
	}

	public double getDeflectionY() {
		double sum = tr + tl + br + bl;
		if (sum == 0)
			return 0;

		double y = tl - bl + tr - br;
		return y / sum * 4;
	}
//	public PtD getDeflection() {
//		double sum = tr + tl + br + bl;
//		if (sum == 0)
//			return null;
//
//		double x = tr - tl + br - bl;
//		double y = tl - bl + tr - br;
//
//		return new PtD(x / sum * 4, y / sum * 4);
//	}

}
