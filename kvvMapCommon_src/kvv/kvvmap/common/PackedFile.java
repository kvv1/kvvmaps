package kvv.kvvmap.common;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class PackedFile {
	
	private final Map<String, Integer> entries = new HashMap<String, Integer>();
	private final File file;
	private final PackedDataFile pdf;
	
	@Override
	public String toString() {
		return "[PackedFile " + file.toString() + "]";
	}
	
	public PackedFile(File file) throws IOException {
		this.file = file;
		this.pdf = new PackedDataFile(file);
		String path = file.getAbsolutePath();
		int idx = path.lastIndexOf('.');
		String dirPath = path.substring(0, idx) + ".dir";
		FileInputStream is = new FileInputStream(dirPath);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
		try {
			while (true) {
				String name = dis.readUTF();
				int off = dis.readInt();
				entries.put(name, off);
			}
		} catch (EOFException e) {
		} finally {
			is.close();
		}
	}

	public synchronized Collection<String> getNames() {
		return entries.keySet();
	}

	public synchronized InputStream getInputStream(String name)
			throws IOException {
		Integer off = entries.get(name);
		if (off == null)
			return null;
		return pdf.getInputStream(off);
	}
}
