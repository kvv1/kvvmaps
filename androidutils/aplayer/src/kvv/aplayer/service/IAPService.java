package kvv.aplayer.service;

import java.util.List;

import kvv.aplayer.folders.Folder;


public interface IAPService {
	List<Folder> getFolders();
	void addListener(APServiceListener listener);
	void removeListener(APServiceListener listener);
	int getCurrentFolder();
	int getFile();
	int getFileCnt();
	String[] getFiles();
	void toFolder(int position);
	void toFile(int position);
	void prev();
	void next();
	void play_pause();
	int getDuration();
	int getCurrentPosition();
	void seek(int seekStep);
	boolean isPlaying();
	void toRandom(int position);
	void setGain(int db);
	void setComprIdx(int n);
	int getGain();
	int getComprIdx();
	void setDBPer100Idx(int n);
	int getDBPer100Idx();
	float getLevel();
	void reload();
}
