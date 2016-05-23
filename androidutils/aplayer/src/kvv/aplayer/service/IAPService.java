package kvv.aplayer.service;

import java.util.List;

import kvv.aplayer.player.Files;
import kvv.aplayer.player.Folders;

public interface IAPService {
	void addListener(APServiceListener listener);

	void removeListener(APServiceListener listener);

	Folders getFolders();

	void toFolder(int position);

	Files getFiles();

	void toFile(int position);

	void prev();

	void next();

	boolean isPlaying();

	void play_pause();

	int getDuration();

	int getCurrentPosition();

	void seekTo(int f);

	void seek(int seekStep);

	void setRandom(boolean random);

	List<String> getMRU();

	void modeChanged();

	void reload();

	void redo();

	void undo();

	void setVisible(boolean vis);

	void addBadSong(String path);

	void delBadSong(String path);

	List<String> getBadSongs();
}
