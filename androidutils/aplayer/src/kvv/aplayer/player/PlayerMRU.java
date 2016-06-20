package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;
import kvv.aplayer.service.MRU;
import android.content.Context;

public abstract class PlayerMRU extends PlayerBadSongs {

	private MRU mru;

	public PlayerMRU(Context context, List<Folder> folders) {
		super(context, folders);
		mru = new MRU(context);
	}

	@Override
	public void toFolder(int folderIdx) {
		super.toFolder(folderIdx);
		Folders folders = getFolders();
		mru.addMRU(folders.folders.get(folderIdx).path);
		onChanged(OnChangedHint.FOLDER_LIST);
	}
	
	public List<String> getMRU() {
		return mru.getMRU();
	}
}
