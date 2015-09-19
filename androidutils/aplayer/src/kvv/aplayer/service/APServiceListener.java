package kvv.aplayer.service;

import kvv.aplayer.player.Player.OnChangedHint;

public interface APServiceListener {
	void onChanged(OnChangedHint hint);
	void onSpeedChanged(boolean hasSpeed, int fromLocation, int fromPlayer);
	void onLoaded();
}
