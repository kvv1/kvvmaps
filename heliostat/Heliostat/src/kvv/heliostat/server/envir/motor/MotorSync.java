package kvv.heliostat.server.envir.motor;

import java.io.IOException;

import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.server.ParamsHolder;

public class MotorSync implements Motor {
	private enum State {
		IDLE(false), GOING_HOME(true);
		boolean fast;

		State(boolean fast) {
			this.fast = fast;
		}
	}

	private final MotorRaw motorRaw;
	private final int idx;

	private boolean posValid;

	private State state;
	private volatile MotorState motorState = new MotorState("unitialized");

	public MotorSync(MotorRaw motorRaw, int idx) {
		this.motorRaw = motorRaw;
		this.idx = idx;
		motorRaw.init();
		setState(State.IDLE);
	}

	@Override
	public void close() {
		motorRaw.close();
	}

	private void setState(State state) {
		this.state = state;
		try {
			motorRaw.setFast(state.fast);
		} catch (IOException e) {
			motorState = new MotorState(e.getMessage());
		}
	}

	@Override
	public void goHome() {
		posValid = false;
		try {
			motorRaw.stop();
			motorRaw.moveIn1N(-ParamsHolder.params.range[idx]);
			setState(State.GOING_HOME);
			//updateState();
		} catch (IOException e) {
			motorState = new MotorState(e.getMessage());
		}
	}

	@Override
	public void go(int pos) {
		if (!posValid)
			return;
		try {
			moveRaw(pos - motorRaw.getPosition());
			//updateState();
		} catch (IOException e) {
			motorState = new MotorState(e.getMessage());
		}
	}

	@Override
	public void moveRaw(int steps) {
		try {
			motorRaw.stop();
			if (steps > 0)
				motorRaw.moveIn2N(steps);
			else if (steps < 0)
				motorRaw.moveIn1N(steps);
			//updateState();
		} catch (IOException e) {
			motorState = new MotorState(e.getMessage());
		}
		setState(State.IDLE);
	}

	@Override
	public void stop() {
		try {
			motorRaw.stop();
			//updateState();
		} catch (IOException e) {
			motorState = new MotorState(e.getMessage());
		}
		setState(State.IDLE);
	}

	@Override
	public void simStep(int ms) {
		try {
			switch (state) {
			case IDLE:
				break;
			case GOING_HOME:
				if (!motorRaw.isRunning()) {
					motorRaw.clearPosition();
					posValid = true;
					//updateState();
					setState(State.IDLE);
				}
				break;
			default:
				break;
			}
			motorRaw.stepSim(ms);
		} catch (IOException e) {
			motorState = new MotorState(e.getMessage());
		}
	}

	@Override
	public MotorState getState() {
		return motorState;
	}

	@Override
	public MotorState updateState() {
		try {
			motorState = motorRaw.getState();
			motorState.posValid = posValid;
		} catch (IOException e) {
			motorState = new MotorState(e.getMessage());
		}
		return motorState;
	}

}
