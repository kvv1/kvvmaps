package kvv.aplayer.service;

import kvv.aplayer.player.IPlayer.OnChangedHint;


public class APServiceListenerAdapter implements APServiceListener {

	@Override
	public void onChanged(OnChangedHint hint) {
	}

	@Override
	public void onSpeedChanged(boolean hasSpeed, int fromLocation,
			int fromPlayer) {
	}
	
	@Override
	public void onLoaded() {
	}

}
