package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Params implements Serializable {
	public int[] stepsPerDegree = { 400, 1000 };
	public AutoMode auto = AutoMode.OFF;
	public int stepMS = 1000;
	public int[] range = { 80000, 80000 };
	public String controllerParams = "COM=COM4\nSENSOR_ADDR=25\nMOTORS_ADDR=25";
	public boolean clock = true;

	public SimParams simParams = new SimParams();
}
