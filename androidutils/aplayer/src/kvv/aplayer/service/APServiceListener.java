package kvv.aplayer.service;

import kvv.aplayer.player.Player.OnChangedHint;


public interface APServiceListener {
	void onChanged(OnChangedHint hint);
	void onLevelChanged(float level);
	void onSpeedChanged(boolean hasSpeed, int fromLocation, int fromPlayer);
	void onLoaded();
}
