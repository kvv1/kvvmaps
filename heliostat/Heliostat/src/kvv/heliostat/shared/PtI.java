package kvv.heliostat.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PtI implements Serializable {
	public int x;
	public int y;

	public PtI() {
	}

	public PtI(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
