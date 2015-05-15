package kvv.heliostat.server.trajectory;

import kvv.heliostat.server.Heliostat;
import kvv.heliostat.shared.PtD;
import kvv.heliostat.shared.PtI;
import kvv.heliostat.shared.Params.AutoMode;

public abstract class TrajectoryBase implements Trajectory {

	@Override
	public final PtI getMotorsPositions(AutoMode auto, PtD deflection,
			PtI motorsPos, double step) {
		if (auto == AutoMode.OFF)
			return null;

		int[] stepsPerDegree = Heliostat.instance.params.stepsPerDegree;

		if (deflection != null) {
			int azPos = (int) (0.5 + (motorsPos.x - deflection.x
					* stepsPerDegree[0]));
			int altPos = (int) (0.5 + (motorsPos.y - deflection.y
					* stepsPerDegree[1]));

			PtI pos = new PtI(azPos, altPos);

			if (Math.sqrt(deflection.x * deflection.x + deflection.y
					* deflection.y) < 1)
				addValidPos(pos);

			return pos;

		}

		if (auto != AutoMode.FULL)
			return null;

		return getExpectedPos(motorsPos, step);

	}

	protected abstract PtI getExpectedPos(PtI currentPos, double step);

	protected abstract void addValidPos(PtI motorsPos);

}
