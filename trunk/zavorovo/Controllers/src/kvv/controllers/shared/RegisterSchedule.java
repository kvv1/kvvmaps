package kvv.controllers.shared;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class RegisterSchedule implements Serializable {
	public ArrayList<ScheduleItem> items = new ArrayList<ScheduleItem>();
	public boolean enabled;

	public int getValue(int minutes) {
		if (items.size() == 0)
			return 0;

		int value = items.get(items.size() - 1).value;
		for (ScheduleItem item : items) {
			if (item.minutes > minutes)
				break;
			value = item.value;
		}
		return value;
	}

	public void add(int minutes, int value) {
		for (int i = 0; i < items.size(); i++) {
			ScheduleItem item = items.get(i);
			if (item.minutes == minutes) {
				if (item.value == value)
					items.remove(i);
				else
					item.value = value;
				return;
			}
			if (item.minutes > minutes) {
				items.add(i, new ScheduleItem(minutes, value));
				return;
			}
		}
		items.add(new ScheduleItem(minutes, value));
	}

	public void compact() {
		if (items.size() == 0)
			return;

		int value = items.get(0).value;

		for (int i = 1; i < items.size();) {
			ScheduleItem item = items.get(i);
			if (item.value == value)
				items.remove(i);
			else
				value = items.get(i++).value;
		}

		if (items.size() > 1 && value == items.get(0).value)
			items.remove(0);
	}
}
