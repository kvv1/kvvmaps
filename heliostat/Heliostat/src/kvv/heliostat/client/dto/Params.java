package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Params implements Serializable {
	public int[] stepsPerDegree = { 10, 10 };
	public AutoMode auto = AutoMode.OFF;
	public int stepMS = 1000;
	public int[] range = { 1000, 1000 };
	public String controllerParams = "";
	
	public SimParams simParams = new SimParams();
}
