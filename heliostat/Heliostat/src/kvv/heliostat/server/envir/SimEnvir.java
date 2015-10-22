package kvv.heliostat.server.envir;

import java.io.IOException;
import java.util.Properties;

import kvv.heliostat.client.dto.Params;
import kvv.heliostat.client.dto.Weather;
import kvv.heliostat.server.envir.motor.Motor;
import kvv.heliostat.server.envir.motor.MotorRawSim;
import kvv.heliostat.server.envir.sensor.SensorSim;
import kvv.stdutils.Utils;

public class SimEnvir extends Envir {

	private static final String WEATHER_FILE = "c:/heliostat/weather.json";
	private final MotorRawSim motorAzimuthRaw = new MotorRawSim();
	private final MotorRawSim motorAltitudeRaw = new MotorRawSim();
	private Motor azMotor = new Motor(motorAzimuthRaw);
	private Motor altMotor = new Motor(motorAltitudeRaw);
	private final SensorSim sensor = new SensorSim(motorAzimuthRaw,
			motorAltitudeRaw);

	private final SimTime simTime = new SimTime();
	
	private boolean shortDay;
	
	public SimEnvir() {

		Weather weather;
		try {
			weather = Utils.jsonRead(WEATHER_FILE, Weather.class);
		} catch (IOException e) {
			weather = new Weather(5, 19, 4, 0, new boolean[30][(19 - 5) * 4]);
		}

		init(azMotor, altMotor, sensor, weather, simTime);
	}

	@Override
	public void paramsChanged(Params params) {
		this.shortDay = params.simParams.shortDay;
	}

	@Override
	public void close() {
	}

	@Override
	public void step(int ms) {
		motors[0].simStep(ms);
		motors[1].simStep(ms);
		simTime.step(ms, shortDay);
	}

	@Override
	public void start() {
	}

	@Override
	public Properties getProps() {
		return new Properties();
	}

	@Override
	public void saveWeather() {
		try {
			Utils.jsonWrite(WEATHER_FILE, weather);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
