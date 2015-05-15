package kvv.heliostat.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SensorState implements Serializable {
	public boolean  valueValid;
	public PtD deflection;
	public int tl;
	public int tr;
	public int bl;
	public int br;

	public SensorState() {
	}

	public SensorState(boolean valueValid, PtD deflection, int tl, int tr, int bl, int br) {
		this.valueValid = valueValid;
		this.deflection = deflection;
		this.tl = tl;
		this.tr = tr;
		this.bl = bl;
		this.br = br;
	}

}
