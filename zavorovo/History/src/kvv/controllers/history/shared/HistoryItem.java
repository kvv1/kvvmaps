package kvv.controllers.history.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HistoryItem implements Serializable {
	public int seconds;
	public Integer value;

	public HistoryItem() {
	}

	public HistoryItem(int seconds, Integer value) {
		this.seconds = seconds;
		this.value = value;
	}
}
