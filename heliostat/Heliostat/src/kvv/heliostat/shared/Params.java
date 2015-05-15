package kvv.heliostat.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Params implements Serializable {
	public enum AutoMode {
		OFF,
		SUN_ONLY,
		FULL
	}
	
	public int[] stepsPerDegree = { 10, 10 };
	public AutoMode auto = AutoMode.OFF;
	
	public boolean clock;
	public int clockRate = 1;
	public int stepMS = 1000;
	public boolean shortDay;
}
