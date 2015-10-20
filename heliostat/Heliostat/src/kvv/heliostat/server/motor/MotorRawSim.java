package kvv.heliostat.server.motor;

import kvv.heliostat.shared.environment.Environment;

public class MotorRawSim implements MotorRaw {

	private final static int SPEED = 100;
	private final static int SPEED1 = 300;

	private int speed = SPEED;

	public int posAbs = (int) (Math.random() * Environment.MAX_STEPS);
	private int dPos;

	private int stepCnt;
	private boolean dir;

	enum State {
		STOPPED, MOVE_IN1, MOVE_IN2,
	}

	private State state = State.STOPPED;

	private int msteps;

	@Override
	public void stepSim(int ms) {

		msteps += ms * speed;
		int steps = Math.min(msteps / 1000, stepCnt);
		msteps %= 1000;

		// int steps = Math.min(ms * speed / 1000, stepCnt);

		switch (state) {
		case STOPPED:
			break;
		case MOVE_IN1:
		case MOVE_IN2:
			if (stepCnt > 0) {
				stepCnt -= steps;
				posAbs += dir ? -steps : steps;
				if (state == State.MOVE_IN1 && posAbs <= 0) {
					posAbs = 0;
					stepCnt = 0;
				} else if (state == State.MOVE_IN2
						&& posAbs >= Environment.MAX_STEPS) {
					posAbs = Environment.MAX_STEPS;
					stepCnt = 0;
				}
			}
			if (stepCnt == 0)
				state = State.STOPPED;
			break;
		default:
			break;
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void moveIn1N(int cnt) {
		dir = cnt < 0;
		stepCnt = cnt;
		if(dir)
			stepCnt = -stepCnt;
		state = State.MOVE_IN1;
	}

	@Override
	public void moveIn2N(int cnt) {
		dir = cnt < 0;
		stepCnt = cnt;
		if(dir)
			stepCnt = -stepCnt;
		state = State.MOVE_IN2;
	}

	@Override
	public int getPosition() {
		return posAbs + dPos;
	}

	@Override
	public void setPosition(int pos) {
		dPos = pos - posAbs;
	}

	@Override
	public boolean getIn1() {
		return posAbs <= 0;
	}

	@Override
	public boolean getIn2() {
		return posAbs >= Environment.MAX_STEPS;
	}

	@Override
	public void stop() {
		stepCnt = 0;
		state = State.STOPPED;
	}

	@Override
	public int getPosAbs() {
		return posAbs;
	}

	@Override
	public void setFast(boolean b) {
		speed = b ? SPEED1 : SPEED;
	}

}
