package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LogItem implements Serializable {
	public int seconds;
	public Integer value;

	public LogItem() {
	}

	public LogItem(int seconds, Integer value) {
		this.seconds = seconds;
		this.value = value;
	}
}
