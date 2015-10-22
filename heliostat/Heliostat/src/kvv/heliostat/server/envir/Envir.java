package kvv.heliostat.server.envir;

import java.util.Properties;

import kvv.heliostat.client.dto.Weather;
import kvv.heliostat.server.envir.motor.Motor;
import kvv.heliostat.server.envir.sensor.Sensor;

public abstract class Envir {

	public Motor[] motors;
	public Sensor sensor;
	public Weather weather;
	public Time time;

	public static Envir instance = new SimEnvir();

	public void init(Motor azMotor, Motor altMotor, Sensor sensor,
			Weather weather, Time time) {
		this.motors = new Motor[] { azMotor, altMotor };
		this.sensor = sensor;
		this.weather = weather;
		this.time = time;
	}

	public abstract void close();

	public abstract void step(int ms);

	public abstract void start();
	
	public abstract void saveWeather();

}
