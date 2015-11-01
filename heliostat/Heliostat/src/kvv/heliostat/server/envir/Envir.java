package kvv.heliostat.server.envir;

import kvv.heliostat.client.dto.Weather;
import kvv.heliostat.server.ParamsHolder;
import kvv.heliostat.server.envir.motor.Motor;
import kvv.heliostat.server.envir.sensor.Sensor;

public abstract class Envir {

	public Motor[] motors;
	public Sensor sensor;
	public Weather weather;
	public Time time;

	public static volatile Envir instance;

	static {
		recreate();
	}

	public synchronized static void recreate() {
		Envir newInst;
		if (ParamsHolder.params.SIM)
			newInst = new SimEnvir();
		else
			newInst = new RealEnvir();
		newInst.start();

		Envir oldInst = instance;
		instance = newInst;

		if (oldInst != null)
			oldInst.close();
	}

	public void init(Motor azMotor, Motor altMotor, Sensor sensor,
			Weather weather, Time time) {
		this.motors = new Motor[] { azMotor, altMotor };
		this.sensor = sensor;
		this.weather = weather;
		this.time = time;
	}

	public abstract void close();

	public abstract void step(int ms);

	protected abstract void start();

	public abstract void saveWeather();

}
