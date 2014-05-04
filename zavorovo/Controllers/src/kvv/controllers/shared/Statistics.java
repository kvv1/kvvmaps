package kvv.controllers.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class Statistics implements Serializable {
	public static class AddrStaistics implements Serializable {
		public int successCnt;
		public int errorCnt;
		public Map<String, Integer> errors = new HashMap<String, Integer>();
	}

	public Map<Integer, AddrStaistics> controllers = new HashMap<Integer, AddrStaistics>();
}
