package kvv.aplayer.service;

public class FileDescriptor implements Comparable<FileDescriptor>{
	public final String path;
	public final String name;
	public final long duration;
	
	public FileDescriptor(String path, String name, long duration) {
		this.path = path;
		this.name = name;
		this.duration = duration;
	}

	@Override
	public int compareTo(FileDescriptor another) {
		return name.compareTo(another.name);
	}
	
}
