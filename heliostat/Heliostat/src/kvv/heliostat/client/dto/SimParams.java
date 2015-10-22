package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SimParams implements Serializable {

	public int clockRate = 1;
	public boolean shortDay;

	public int MAX_STEPS = 80000;
	public int MIN_AZIMUTH = -60;
	public int MAX_AZIMUTH = 60;
	public int MIN_ALTITUDE = -10;
	public int MAX_ALTITUDE = 55;

	public double[][] azDeg2Steps = { { MIN_AZIMUTH, 20, MAX_AZIMUTH },
			{ 0, MAX_STEPS / 2, MAX_STEPS } };

	public double[][] altDeg2Steps = { { MIN_ALTITUDE, 30, MAX_ALTITUDE },
			{ 0, MAX_STEPS / 2, MAX_STEPS } };
}
