package kvv.heliostat.server.envir.motor;

import java.io.IOException;

import kvv.controllers.controller.IController;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.server.ParamsHolder;

public class MotorRawT3 implements MotorRaw {

	private final static int REG_POSITION_OFF = 0;
	private final static int REG_COUNTER_OFF = 2;
	private final static int REG_DIR_OFF = 5;
	private final static int REG_SPEED_OFF = 6;
	private final static int REG_CMD_OFF = 8;
	private final static int REG_IN_OFF = 9;
	private final static int REG_RUNNING_OFF = 10;

	private final static int REG_MAX_OFF = 16;

	private static final int CMD_STOP = 0;
	private static final int CMD_MOVE_IN1_N = 1;
	private static final int CMD_MOVE_IN2_N = 2;

	private final IController controller;
	private final int regBase;

	public MotorRawT3(IController controller, int motorNo) {
		this.controller = controller;
		regBase = motorNo == 0 ? 64 : 80;
	}

	private int getAddr() {
		return Integer.parseInt(ParamsHolder.controllerParams.getProperty(
				"MOTORS_ADDR", "0"));
	}

	private void setReg16(int reg, int val) throws IOException {
		controller.setReg(getAddr(), reg, val);
	}

	public int getReg16(int reg) throws IOException {
		return controller.getReg(getAddr(), reg);
	}

	private void setReg32(int reg, int val) throws IOException {
		controller.setRegs(getAddr(), reg, val >> 16, val);
	}

	public int getReg32(int reg) throws IOException {
		int[] vals = controller.getRegs(getAddr(), reg, 2);
		return (vals[0] << 16) + (vals[1] & 0xFFFF);
	}

	@Override
	public void init() {

		// TODO Auto-generated method stub
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveIn1N(int cnt) throws IOException {
		setReg32(regBase + REG_COUNTER_OFF, Math.abs(cnt));
		setReg16(regBase + REG_DIR_OFF, cnt >= 0 ? 0 : 1);
		setReg16(regBase + REG_CMD_OFF, CMD_MOVE_IN1_N);
	}

	@Override
	public void moveIn2N(int cnt) throws IOException {
		setReg32(regBase + REG_COUNTER_OFF, Math.abs(cnt));
		setReg16(regBase + REG_DIR_OFF, cnt >= 0 ? 0 : 1);
		setReg16(regBase + REG_CMD_OFF, CMD_MOVE_IN2_N);
	}

	@Override
	public int getPosition() throws IOException {
		int pos = getReg32(regBase + REG_POSITION_OFF);
		return pos;
	}

	@Override
	public void clearPosition() throws IOException {
		setReg32(regBase + REG_POSITION_OFF, 0);
	}

	@Override
	public boolean getIn1() throws IOException {
		return getReg16(regBase + REG_IN_OFF) != 0;
	}

	@Override
	public boolean getIn2() throws IOException {
		return false;
	}

	@Override
	public void stop() throws IOException {
		setReg16(regBase + REG_CMD_OFF, CMD_STOP);
	}

	@Override
	public Integer getPosAbs() throws IOException {
		return null;
	}

	@Override
	public void setFast(boolean b) throws IOException {
		setReg16(regBase + REG_SPEED_OFF, b ? 10 : 50);
	}

	@Override
	public void stepSim(int ms) {
	}

	@Override
	public boolean isRunning() throws IOException {
		return getReg16(regBase + REG_RUNNING_OFF) != 0;
	}

	@Override
	public MotorState getState() throws IOException {
		int[] vals = controller.getRegs(getAddr(), regBase, REG_MAX_OFF);

		return new MotorState((vals[REG_POSITION_OFF] << 16)
				+ (vals[REG_POSITION_OFF + 1] & 0xFFFF), false,
				vals[REG_RUNNING_OFF] != 0, vals[REG_IN_OFF] != 0, false, null);

		// return new MotorState(getPosition(), false, isRunning(), getIn1(),
		// getIn2(), getPosAbs());
	}

}
