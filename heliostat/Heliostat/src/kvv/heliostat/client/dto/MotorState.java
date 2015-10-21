package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MotorState implements Serializable {

	public int pos;
	public boolean posValid;
	public boolean home;
	public boolean end;
	public Integer posAbs;

	public MotorState() {
	}

	public MotorState(int pos, boolean posValid, boolean home,
			boolean end, Integer posAbs) {
		this.pos = pos;
		this.posValid = posValid;
		this.home = home;
		this.end = end;
		this.posAbs = posAbs;
	}
}
