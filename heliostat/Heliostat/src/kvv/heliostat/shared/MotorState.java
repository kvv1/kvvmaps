package kvv.heliostat.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MotorState implements Serializable {

	public static class MotorRawSimState implements Serializable {
		public int max;
		public int pos;

		public MotorRawSimState() {
		}

		public MotorRawSimState(int max, int pos) {
			this.max = max;
			this.pos = pos;
		}

	}

	public Integer max;
	public int pos;
	public boolean posValid;
	public boolean home;
	public boolean end;
	public int dir;
	public MotorRawSimState motorRawSimState;

	public MotorState() {
	}

	public MotorState(Integer max, int pos, boolean posValid, boolean home,
			boolean end, int dir, MotorRawSimState motorRawSimState) {
		this.max = max;
		this.pos = pos;
		this.posValid = posValid;
		this.home = home;
		this.end = end;
		this.dir = dir;
		this.motorRawSimState = motorRawSimState;
	}
}
