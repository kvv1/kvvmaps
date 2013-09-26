package kvv.controllers.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Log implements Serializable{
	public HashMap<Register, ArrayList<LogItem>> items = new HashMap<Register, ArrayList<LogItem>>();
}
