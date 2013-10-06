package kvv.controllers.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import kvv.controllers.shared.history.HistoryItem;

@SuppressWarnings("serial")
public class Schedule implements Serializable {
	public Date date = new Date();
	public HashMap<String, RegisterSchedule> map = new HashMap<String, RegisterSchedule>();
	public HashMap<String, ArrayList<HistoryItem>> history = new HashMap<String, ArrayList<HistoryItem>>();
}
