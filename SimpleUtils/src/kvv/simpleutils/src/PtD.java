package kvv.simpleutils.src;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PtD implements Serializable {
	public double x;
	public double y;

	public PtD() {
	}

	public PtD(double x, double y) {
		this.x = x;
		this.y = y;
	}
}
