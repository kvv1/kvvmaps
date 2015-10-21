package kvv.heliostat.client.model;

import kvv.heliostat.client.dto.HeliostatState;

public interface View {
	void updateView(HeliostatState state);
}
