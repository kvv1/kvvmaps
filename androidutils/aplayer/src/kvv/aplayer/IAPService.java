package kvv.aplayer;

import java.util.List;


public interface IAPService {
	List<Folder> getFolders();
	void addListener(APServiceListener listener);
	void removeListener(APServiceListener listener);
	int getCurrentFolder();
	int getFile();
	void toFolder(int position);
	void toFile(int position);
	void prev();
	void next();
	void play_pause();
	int getDuration();
	int getCurrentPosition();
	void seek(int seekStep);
	boolean isPlaying();
	List<Bookmark> getBookmarks();
	void addBookmark();
	void delBookmark(Bookmark bookmark);
	void toBookmark(Bookmark bookmark);
	void toRandom(int position);
	void setGain(int db);
	void setCompr(boolean b );
	int getGain();
	boolean getCompr();
}
