package kvv.controllers.shared;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class History implements Serializable {
	public ArrayList<HistoryItem> items = new ArrayList<HistoryItem>();
}
