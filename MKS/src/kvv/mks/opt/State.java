package kvv.mks.opt;

import kvv.mks.rot.Rot;

public class State {
	public Rot rot;

	public double dx;
	public double dy;
	public double dz;

	public State(State state) {
		this(state.rot.getCopy(), state.dx, state.dy, state.dz);
	}

	public State(Rot rot, double dx, double dy, double dz) {
		this.rot = rot;
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	@Override
	public String toString() {
		return String.format("%s %7.3f %7.3f %7.3f",
				rot.toString(), dx, dy, dz);
	}

}