package kvv.heliostat.server.trajectory;

import kvv.heliostat.shared.Params.AutoMode;
import kvv.heliostat.shared.PtD;
import kvv.heliostat.shared.PtI;

public interface Trajectory {

	PtI getMotorsPositions(AutoMode auto, PtD deflection, PtI motorsPos, double step);
	void clearHistory();
}
