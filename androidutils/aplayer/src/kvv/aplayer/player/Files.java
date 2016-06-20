package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.FileDescriptor;

public class Files {
	public final List<FileDescriptor> files;
	public final int curFile;

	public Files(List<FileDescriptor> files, int curFile) {
		this.files = files;
		this.curFile = curFile;
	}

	public int getIndex(String path) {
		for (int i = 0; i < files.size(); i++)
			if (files.get(i).equals(path))
				return i;
		return -1;
	}

	public FileDescriptor getFile() {
		if (curFile < 0 || curFile >= files.size())
			return null;
		return files.get(curFile);
	}

}