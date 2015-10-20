package kvv.heliostat.server.motor;

import kvv.heliostat.shared.MotorState;

public class Motor {

	private final MotorRaw motorRaw;

	private boolean posValid;

	enum State {
		IDLE(false), GOING_HOME(true), GOING_HOME1(true), GOING_HOME2(true);
		boolean fast;

		State(boolean fast) {
			this.fast = fast;
		}
	}

	private State state;

	public Motor(MotorRaw motorRaw) {
		this.motorRaw = motorRaw;
		motorRaw.init();
		setState(State.IDLE);
	}

	public void setState(State state) {
		this.state = state;
		motorRaw.setFast(state.fast);
	}

	public void goHome() {
		posValid = false;
		setState(State.GOING_HOME);
	}

	public void go(int pos) {
		if (!posValid)
			return;
		motorRaw.stop();
		moveRaw(pos - motorRaw.getPosition());
	}

	public void moveRaw(int steps) {
		motorRaw.stop();
		setState(State.IDLE);

		if (steps > 0) {
			motorRaw.setDir(false);
			motorRaw.setStepNumber(steps);
			motorRaw.moveIn2N();
		} else if (steps < 0) {
			motorRaw.setDir(true);
			motorRaw.setStepNumber(-steps);
			motorRaw.moveIn1N();
		}
	}

	public void stop() {
		motorRaw.stop();
		setState(State.IDLE);
	}

	public MotorState getState() {
		int cnt = motorRaw.getStepsCounter();
		boolean dir = motorRaw.getDir();
		return new MotorState(motorRaw.getPosition(), posValid,
				motorRaw.getIn1(), motorRaw.getIn2(), cnt == 0 ? 0 : dir ? -1
						: 1, motorRaw.getState());
	}

	public void simStep(int ms) {
		switch (state) {
		case IDLE:
			break;
		case GOING_HOME:
			motorRaw.stop();
			if (motorRaw.getIn1()) {
				motorRaw.setDir(false);
				motorRaw.setStepNumber(1000000);
				motorRaw.moveIn2N();
			}
			setState(State.GOING_HOME1);
			break;
		case GOING_HOME1:
			if (!motorRaw.getIn1()) {
				motorRaw.stop();
				motorRaw.setDir(true);
				motorRaw.setStepNumber(1000000);
				motorRaw.moveIn1N();
				setState(State.GOING_HOME2);
			}
			break;
		case GOING_HOME2:
			if (motorRaw.getIn1()) {
				motorRaw.stop();
				motorRaw.setPosition(0);
				posValid = true;
				setState(State.IDLE);
			}
			break;
		}
		motorRaw.stepSim(ms);
	}

}
