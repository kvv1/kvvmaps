package kvv.controllers.shared;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class RegisterSchedule implements Serializable {
	public ArrayList<ScheduleItem> items = new ArrayList<ScheduleItem>();
	public boolean enabled;

	public int getValue(int minutes) {
		int value = 0;
		for (ScheduleItem item : items)
			value = item.value;
		for (ScheduleItem item : items) {
			if (item.minutes > minutes)
				break;
			value = item.value;
		}
		return value;
	}
}
