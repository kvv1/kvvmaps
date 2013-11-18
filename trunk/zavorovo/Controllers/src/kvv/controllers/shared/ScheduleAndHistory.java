package kvv.controllers.shared;

import java.io.Serializable;

import kvv.controllers.history.shared.History;

@SuppressWarnings("serial")
public class ScheduleAndHistory implements Serializable {
	public Schedule schedule;
	public History history;

	public ScheduleAndHistory() {
	}

	public ScheduleAndHistory(Schedule schedule, History history) {
		this.schedule = schedule;
		this.history = history;
	}
}
