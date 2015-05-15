package kvv.heliostat.client;

import kvv.heliostat.shared.HeliostatState;

public interface View {
	void updateView(HeliostatState state);
}
