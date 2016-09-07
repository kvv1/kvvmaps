package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.BadSongs;
import kvv.aplayer.service.Folder;
import android.content.Context;

public abstract class PlayerBadSongs extends Player2 {

	private BadSongs badSongs;

	public PlayerBadSongs(Context context, List<Folder> folders) {
		super(context, folders);
		badSongs = new BadSongs(context);
	}

	public void addBadSong(String path) {
		badSongs.addBadSong(path);
		for (PlayerListener l : listeners)
			l.folderChanged();
	}

	public void delBadSong(String path) {
		badSongs.delBadSong(path);
		for (PlayerListener l : listeners)
			l.folderChanged();
	}

	public List<String> getBadSongs() {
		return badSongs.getBadSongs();
	}

	@Override
	protected boolean isBadSong(String path) {
		return badSongs.getBadSongs().contains(path);
	}

}
