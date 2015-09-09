package kvv.aplayer.service;

public interface APServiceListener {
	void onChanged();

	void onBookmarksChanged();
	
	void onRandomChanged();
	
	void onSpeedChanged(boolean hasSpeed, int fromLocation, int fromPlayer);
}
