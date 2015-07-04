package kvv.heliostat.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SensorState implements Serializable {
	public boolean valueValid;
	public PtD deflection;
	public int tl;
	public int tr;
	public int bl;
	public int br;

	public SensorState() {
	}

	public SensorState(boolean valueValid, PtD deflection, int tl, int tr,
			int bl, int br) {
		init(valueValid, deflection, tl, tr, bl, br);
	}

	public SensorState(int tl, int tr, int bl, int br) {
		double sum = tr + tl + br + bl;
		double x = tr - tl + br - bl;
		double y = tl - bl + tr - br;

		PtD deflection = null;

		if (sum != 0)
			deflection = new PtD(x / sum * 4, y / sum * 4);

		init(sum > 100, deflection, (int) tl, (int) tr, (int) bl, (int) br);
	}

	private void init(boolean valueValid, PtD deflection, int tl, int tr,
			int bl, int br) {
		this.valueValid = valueValid;
		this.deflection = deflection;
		this.tl = tl;
		this.tr = tr;
		this.bl = bl;
		this.br = br;
	}

}
