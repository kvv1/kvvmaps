package kvv.aplayer.service;

import java.util.List;

public final class Folder {
	public final String path;
	public final int indent;

	public List<FileDescriptor> _files;
	public List<FileDescriptor> filesToPlay;

	public Long seed;

	public Folder(String pathName, int indent, List<FileDescriptor> files) {
		this.path = pathName;
		this.indent = indent;
		this._files = files;
	}

	public String getDisplayName() {
		return path + (seed != null ? " RND" : "");
	}
}