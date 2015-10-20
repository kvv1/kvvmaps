package kvv.heliostat.server.motor;

import java.io.IOException;

import kvv.heliostat.server.Heliostat;
import kvv.heliostat.server.controller.IController;
import kvv.heliostat.shared.MotorState.MotorRawSimState;

public class MotorRawT3 implements MotorRaw {

	private static final int REG_STEPS_COUNTER = 0;
	private static final int REG_POSITION = 0;
	private static final int REG_IN1 = 0;
	private static final int REG_IN2 = 0;
	private static final int REG_CMD = 0;

	private static final int CMD_STOP = 0;
	private static final int CMD_MOVE_IN1_N = 1;
	private static final int CMD_MOVE_IN2_N = 2;

	private final IController controller;

	public MotorRawT3(IController controller) {
		this.controller = controller;
	}

	private int getAddr() {
		return Integer.parseInt(Heliostat.instance.controllerParams
				.getProperty("MOTORS_ADDR", "0"));

	}

	private void setReg(int reg, int val) {
		try {
			controller.setReg(getAddr(), reg, val);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getReg(int reg) {
		try {
			return controller.getReg(getAddr(), reg);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public void moveIn1N(int cnt) {
		setReg(REG_CMD, CMD_MOVE_IN1_N);
	}

	@Override
	public void moveIn2N(int cnt) {
		setReg(REG_CMD, CMD_MOVE_IN2_N);
	}

	@Override
	public int getPosition() {
		return getReg(REG_POSITION);
	}

	@Override
	public void setPosition(int pos) {
		setReg(REG_POSITION, pos);
	}

	@Override
	public boolean getIn1() {
		return getReg(REG_IN1) != 0;
	}

	@Override
	public boolean getIn2() {
		return getReg(REG_IN2) != 0;
	}

	@Override
	public void stop() {
		setReg(REG_CMD, CMD_STOP);
	}

	@Override
	public MotorRawSimState getState() {
		return new MotorRawSimState(getPosition());
	}

	@Override
	public void setFast(boolean b) {
		// TODO Auto-generated method stub
	}

	@Override
	public void stepSim(int ms) {
	}

}
