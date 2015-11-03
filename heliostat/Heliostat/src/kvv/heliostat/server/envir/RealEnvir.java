package kvv.heliostat.server.envir;

import java.io.IOException;

import kvv.controllers.controller.Controller;
import kvv.controllers.controller.adu.ADUTransceiver;
import kvv.controllers.controller.adu.TCPTransceiver;
import kvv.heliostat.server.ParamsHolder;
import kvv.heliostat.server.envir.motor.Motor;
import kvv.heliostat.server.envir.motor.MotorRaw;
import kvv.heliostat.server.envir.motor.MotorRawT3;
import kvv.heliostat.server.envir.motor.MotorSync;
import kvv.heliostat.server.envir.sensor.SensorImpl;

public class RealEnvir extends Envir {

	public final Controller controller = new Controller() {
		private String com = "";

		public synchronized byte[] send(int addr, byte[] request)
				throws IOException {
			String com = ParamsHolder.controllerParams.getProperty("COM", "");
			if (modbusLine == null || !this.com.equals(com)) {
				this.com = com;
//				setModbusLine(new ADUTransceiver(
//						new COMTransceiver(com, 500)));
				setModbusLine(new ADUTransceiver(
						new TCPTransceiver("192.168.1.17", 8899, 1000)));
			}
			return super.send(addr, request);
		}
	};
	private final MotorRaw motorAzimuthRaw = new MotorRawT3(controller, 0);
	private final MotorRaw motorAltitudeRaw = new MotorRawT3(controller, 1);

	private Motor azMotor = new MotorSync(motorAzimuthRaw, 0);
	private Motor altMotor = new MotorSync(motorAltitudeRaw, 1);

	private final SensorImpl sensor = new SensorImpl(controller);

	private final RealTime realTime = new RealTime();

	public RealEnvir() {
		init(azMotor, altMotor, sensor, null, realTime);
	}

//	private void checkInterrupted__() throws IntExc {
//		if (azMotor != null)
//			azMotor._checkInterrupted();
//		if (altMotor != null)
//			altMotor._checkInterrupted();
//	}

	@Override
	public void close() {
		sensor.close();
		motors[0].close();
		motors[1].close();
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
	public void saveWeather() {
	}

}
