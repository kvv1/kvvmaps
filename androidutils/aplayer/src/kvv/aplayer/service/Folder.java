package kvv.aplayer.service;

import java.util.List;

public class Folder {
	public final String path;
	public final String shortName;
	public final int indent;

	public List<FileDescriptor> _files;
	public List<FileDescriptor> filesToPlay;

	public boolean random;

	public Folder(String pathName, int indent, List<FileDescriptor> files) {
		this.path = pathName;
		this.indent = indent;
		this._files = files;

		String[] path = pathName.split("/");
		String txt = "";
		for (int i = 0; i < indent + 1; i++) {
			int idx = path.length - indent - 1 + i;
			if (txt.length() != 0)
				txt += "/";
			txt += path[idx];
		}
		shortName = path[path.length - 1];
		
		filesToPlay = _files;
	}

	public String getDisplayName() {
		return path + (random ? " RND" : "");
	}
}