package kvv.aplayer.service;

public class File1 implements Comparable<File1>{
	public final String path;
	public final String name;
	public final long duration;
	
	public File1(String path, String name, long duration) {
		this.path = path;
		this.name = name;
		this.duration = duration;
	}

	@Override
	public int compareTo(File1 another) {
		return name.compareTo(another.name);
	}
	
}
