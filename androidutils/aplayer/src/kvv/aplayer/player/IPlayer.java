package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;

public interface IPlayer {

	void pause();

	void play();

	void prev();

	void next();

	boolean isPlaying();

	void setGain(int int1);

	void setCompr(int i);

	void setDbPer100(float f);

	int getCurrentFolder();

	List<Folder> getFolders();

	void toFolder(int position, int curFile, int curPos);

	void makeRandom(int position);

	void toFile(int position);

	int getDuration();

	int getCurrentPosition();

	void seek(int seekStep);

	int getFile();

	int getGain();

	float getIndicatorLevel();

	void setVisible(boolean vis);

	void setSpeedKMH(float speed);

	void close();

	void onChanged(OnChangedHint hint);

	public enum OnChangedHint {
		FOLDER, FILE, POSITION
	}

}

