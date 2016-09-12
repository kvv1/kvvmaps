package kvv.aplayer.service;

import java.util.List;

import kvv.aplayer.player.Files;
import kvv.aplayer.player.Folders;
import kvv.aplayer.player.Player.PlayerListener;

public interface IAPService {
	void addListener(PlayerListener listener);

	void removeListener(PlayerListener listener);

	Folders getFolders();

	void toFolder(int position);

	Files getFiles();

	void toFile(int position);

	void prev();

	void next();

	boolean isPlaying();

	void play();

	void pause();

	int getDuration();

	int getCurrentPosition();

	void seekTo(int f);

	void setRandom();

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
