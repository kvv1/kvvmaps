package kvv.controllers.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class History implements Serializable{
	public HashMap<Register, ArrayList<HistoryItem>> items = new HashMap<Register, ArrayList<HistoryItem>>();
}
