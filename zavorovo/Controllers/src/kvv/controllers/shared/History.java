package kvv.controllers.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class History implements Serializable {
	public HashMap<String, ArrayList<HistoryItem>> items = new HashMap<String, ArrayList<HistoryItem>>();
}
