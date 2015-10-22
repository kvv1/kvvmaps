package kvv.heliostat.server.envir;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import kvv.heliostat.client.dto.Params;
import kvv.heliostat.server.controller.Controller;
import kvv.heliostat.server.controller.adu.ADUTransceiver;
import kvv.heliostat.server.controller.adu.PacketTransceiver;
import kvv.heliostat.server.envir.motor.Motor;
import kvv.heliostat.server.envir.motor.MotorRaw;
import kvv.heliostat.server.envir.motor.MotorRawT3;
import kvv.heliostat.server.envir.sensor.SensorImpl;

public class RealEnvir extends Envir {

	private final Controller controller = new Controller();
	private final MotorRaw motorAzimuthRaw = new MotorRawT3(controller);
	private final MotorRaw motorAltitudeRaw = new MotorRawT3(controller);
	private Motor azMotor = new Motor(motorAzimuthRaw);
	private Motor altMotor = new Motor(motorAltitudeRaw);
	private final SensorImpl sensor = new SensorImpl(controller);
	public Properties controllerParams = new Properties();

	private final RealTime realTime = new RealTime();
	
	public RealEnvir() {
		init(azMotor, altMotor, sensor, null, realTime);
	}

	@Override
	public void paramsChanged(Params params) {
		try {
			controllerParams.load(new StringReader(params.controllerParams));
			String com = controllerParams.getProperty("COM", "");
			controller.setModbusLine(new ADUTransceiver(new PacketTransceiver(
					com, 500)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		sensor.close();
		controller.close();
	}

	@Override
	public void step(int ms) {
		motors[0].simStep(ms);
		motors[1].simStep(ms);
	}

	@Override
	public void start() {
		sensor.start();
	}

	@Override
	public Properties getProps() {
		return controllerParams;
	}

	@Override
	public void saveWeather() {
	}

}
