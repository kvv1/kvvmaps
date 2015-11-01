package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HeliostatState implements Serializable{
	public int reqNo;
	public MotorState[] motorState;
	public SensorState sensorState;
	public DayTime dayTime;
	public Params params;
	public double[][] azData;
	public double[][] altData;

	public HeliostatState() {
	}

	public HeliostatState(MotorState[] motorState, SensorState sensorState,
			Params params, DayTime dayTime, double[][] azData,
			double[][] altData) {
		this.motorState = motorState;
		this.sensorState = sensorState;
		this.params = params;
		this.dayTime = dayTime;
		this.azData = azData;
		this.altData = altData;
	}
}