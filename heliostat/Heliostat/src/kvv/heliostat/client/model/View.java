package kvv.heliostat.client.model;

import kvv.heliostat.shared.HeliostatState;

public interface View {
	void updateView(HeliostatState state);
}
