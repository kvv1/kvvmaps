package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ScheduleItem implements Serializable {
	public int minutes;
	public int value;

	public ScheduleItem() {
	}

	public ScheduleItem(int minutes, int value) {
		this.minutes = minutes;
		this.value = value;
	}

}
