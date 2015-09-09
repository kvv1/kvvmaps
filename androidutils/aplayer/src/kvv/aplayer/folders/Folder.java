package kvv.aplayer.folders;

import java.io.File;

public class Folder {
	public final String path;
	public final String shortName;
	public final int indent;
	public final String displayName;
	
	public File[] files;

	public Folder(String pathName, int indent, File[] files) {
		this.path = pathName;
		this.indent = indent;
		this.files = files;

		String[] path = pathName.split("/");
		String txt = "";
		for (int i = 0; i < indent + 1; i++) {
			int idx = path.length - indent - 1 + i;
			if(txt.length() != 0)
				txt += "/";
			txt += path[idx];
		}
		displayName = txt;
		shortName=path[path.length-1];
	}
}