package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;

public class Folders {
	public final List<Folder> folders;
	public int curFolder;
	public Folders(List<Folder> folders, int curFolder) {
		this.folders = folders;
		this.curFolder = curFolder;
	}
	
	public int getIndex(String path) {
		for(int i = 0; i < folders.size(); i++)
			if(folders.get(i).path.equals(path))
				return i;
		return -1;
	}
	
	public Folder getFolder() {
		if(curFolder < 0)
			return null;
		return folders.get(curFolder);
	}
}