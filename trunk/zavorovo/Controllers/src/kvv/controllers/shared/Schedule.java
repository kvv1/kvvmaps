package kvv.controllers.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Schedule implements Serializable {
	public boolean enabled;
	public String[] lines;
	public String text;
	public Date date;
	public HashMap<Register, RegisterSchedule> map = new HashMap<Register, RegisterSchedule>();

	public Schedule() {
	}

	public Schedule(boolean enabled) {
		this.enabled = enabled;
	}

}
