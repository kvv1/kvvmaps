package kvv.aplayer.service;

import java.util.List;


public interface IAPService {
	List<Folder> getFolders();
	void addListener(APServiceListener listener);
	void removeListener(APServiceListener listener);
	int getCurrentFolder();
	int getFile();
	int getFileCnt();
	FileDescriptor[] getFiles();
	void toFolder(int position);
	void toFile(int position);
	void prev();
	void next();
	void play_pause();
	int getDuration();
	int getCurrentPosition();
	void seekTo(int f);
	void seek(int seekStep);
	boolean isPlaying();
	void toRandom(int position);
	void modeChanged();
	void reload();
	void redo();
	void undo();
	void setVisible(boolean vis);
}
