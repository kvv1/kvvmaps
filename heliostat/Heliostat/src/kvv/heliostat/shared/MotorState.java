package kvv.heliostat.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MotorState implements Serializable {

	public static class MotorRawSimState implements Serializable {
		//public int max;
		public int pos;

		public MotorRawSimState() {
		}

		public MotorRawSimState(/*int max,*/ int pos) {
			//this.max = max;
			this.pos = pos;
		}

	}

	public int pos;
	public boolean posValid;
	public boolean home;
	public boolean end;
	public MotorRawSimState motorRawSimState;

	public MotorState() {
	}

	public MotorState(int pos, boolean posValid, boolean home,
			boolean end, MotorRawSimState motorRawSimState) {
		this.pos = pos;
		this.posValid = posValid;
		this.home = home;
		this.end = end;
		this.motorRawSimState = motorRawSimState;
	}
}
