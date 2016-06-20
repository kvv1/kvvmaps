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
		onChanged(OnChangedHint.FOLDER);
		onChanged(OnChangedHint.FILE);
	}

	public void delBadSong(String path) {
		badSongs.delBadSong(path);
		onChanged(OnChangedHint.FOLDER);
		onChanged(OnChangedHint.FILE);
	}

	public List<String> getBadSongs() {
		return badSongs.getBadSongs();
	}

	@Override
	protected boolean isBadSong(String path) {
		return badSongs.getBadSongs().contains(path);
	}

}
