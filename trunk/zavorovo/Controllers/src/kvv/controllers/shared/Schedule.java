package kvv.controllers.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Schedule implements Serializable {
	public Date date = new Date();
	public HashMap<String, RegisterSchedule> map = new HashMap<String, RegisterSchedule>();
}
