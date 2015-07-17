package kvv.heliostat.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HeliostatState implements Serializable {
	public MotorState[] motorState;
	public SensorState sensorState;
	public int day;
	public String dayS;
	public double time;
	public String timeS;
	public Params params;
	public double[][] azData;
	public double[][] altData;

	public boolean sun;

	public HeliostatState() {
	}

	public HeliostatState(MotorState[] motorState, SensorState sensorState,
			Params params, int day, String dayS, double time, String timeS,
			double[][] azData, double[][] altData,
			boolean sun) {
		this.motorState = motorState;
		this.sensorState = sensorState;
		this.params = params;
		this.time = time;
		this.timeS = timeS;
		this.day = day;
		this.dayS = dayS;
		this.azData = azData;
		this.altData = altData;
		this.sun = sun;
	}
}