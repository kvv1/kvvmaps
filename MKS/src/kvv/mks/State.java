package kvv.mks;

public class State {
	public double ax;
	public double ay;
	public double az;

	public double dx;
	public double dy;
	public double dz;

	
	public State() {
	}

	public State(State state) {
		this(state.ax, state.ay, state.az, state.dx, state.dy, state.dz);
	}
	
	public State(double ax, double ay, double az, double dx, double dy,
			double dz) {
		this.ax = ax;
		this.ay = ay;
		this.az = az;
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	@Override
	public String toString() {
		return String.format("%7.3f %7.3f %7.3f %7.3f %7.3f %7.3f",
				Util.r2g(ax), Util.r2g(ay), Util.r2g(az), dx, dy, dz);
	}
	
	String toStrNeg() {
		return String.format("%7.3f %7.3f %7.3f %7.3f %7.3f %7.3f",
				-Util.r2g(ax), -Util.r2g(ay), -Util.r2g(az), -dx, -dy, -dz);
	}
}