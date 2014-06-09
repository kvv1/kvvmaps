package kvv.aplayer;

public class Bookmark {
	public String folder;
	public String track;
	public int time;
	public int duration;

	public Bookmark(String folder, String track, int duration, int time) {
		this.folder = folder;
		this.track = track;
		this.duration = duration;
		this.time = time;
	}
}
