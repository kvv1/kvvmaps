package kvv.heliostat.server.motor;

import kvv.heliostat.shared.MotorState;

public abstract class Motor {

	private final MotorRaw motorRaw;

	private boolean posValid;

	enum State {
		IDLE(false), CALIBRATING(true), CALIBRATING1(true), CALIBRATING2(true), CALIBRATING3(true), GOING_HOME(true), GOING_HOME1(true), GOING_HOME2(true);
		boolean fast;
		State(boolean fast) {
			this.fast = fast;
		}
	}

	private State state;

	protected abstract void calibrated(int max);


	public Motor(MotorRaw motorRaw) {
		this.motorRaw = motorRaw;
		motorRaw.init();
		setState(State.IDLE);
	}

	public void setState(State state) {
		this.state = state;
		motorRaw.setFast(state.fast);
	}
	
	public void calibrate() {
		posValid = false;
		setState(State.CALIBRATING);
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
		case CALIBRATING:
			motorRaw.setFast(true);
			motorRaw.stop();
			if (motorRaw.getIn2()) {
				motorRaw.setDir(true);
				motorRaw.setStepNumber(1000000);
				motorRaw.moveIn1N();
			}
			setState(State.CALIBRATING1);
			break;
		case CALIBRATING1:
			if (!motorRaw.getIn2()) {
				motorRaw.stop();
				motorRaw.setDir(false);
				motorRaw.setStepNumber(1000000);
				motorRaw.moveIn2N();
				setState(State.CALIBRATING2);
			}
			break;
		case CALIBRATING2:
			if (motorRaw.getIn2()) {
				motorRaw.stop();
				motorRaw.setPosition(0);
				motorRaw.setDir(true);
				motorRaw.setStepNumber(1000000);
				motorRaw.moveIn1N();
				setState(State.CALIBRATING3);
			}
			break;
		case CALIBRATING3:
			if (motorRaw.getIn1()) {
				motorRaw.stop();
				int max = -motorRaw.getPosition();
				motorRaw.setPosition(0);
				posValid = true;
				setState(State.IDLE);
				calibrated(max);
			}
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
