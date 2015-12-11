package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HistoryItem implements Serializable {
	public int seconds;
	public Integer value;
	public String name;

	public HistoryItem() {
	}

	public HistoryItem(int seconds) {
		this(seconds, null, null);
	}
	
	public HistoryItem(int seconds, String name, Integer value) {
		this.name = name;
		this.seconds = seconds;
		this.value = value;
	}
}
