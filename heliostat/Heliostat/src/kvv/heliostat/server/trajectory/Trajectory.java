package kvv.heliostat.server.trajectory;

import kvv.heliostat.shared.Params.AutoMode;
import kvv.simpleutils.src.PtD;
import kvv.simpleutils.src.PtI;

public interface Trajectory {

	PtI getMotorsPositions(AutoMode auto, PtD deflection, PtI motorsPos, double step);
	void clearHistory();
}
