package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MotorState implements Serializable {

	public int pos;
	public boolean posValid;
	public boolean running;
	public boolean home;
	public boolean end;
	public Integer posAbs;

	public String error;
	
	public MotorState() {
	}

	public MotorState(String err) {
		this.error = err;
	}
	
	public MotorState(int pos, boolean posValid, boolean running, boolean home,
			boolean end, Integer posAbs) {
		this.pos = pos;
		this.posValid = posValid;
		this.running = running;
		this.home = home;
		this.end = end;
		this.posAbs = posAbs;
	}
}
