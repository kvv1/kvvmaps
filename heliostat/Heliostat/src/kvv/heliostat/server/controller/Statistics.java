package kvv.heliostat.server.controller;

import java.util.HashMap;
import java.util.Map;

class AddrStaistics {
	public int successCnt;
	public int errorCnt;
	public Map<String, Integer> errors = new HashMap<String, Integer>();
}

public class Statistics {
	private Map<Integer, AddrStaistics> controllers = new HashMap<Integer, AddrStaistics>();

	public synchronized void addSuccess(int addr) {
		getAddrStaistics(addr).successCnt++;
	}

	public synchronized void addError(int addr, String text) {
		AddrStaistics addrStaistics = getAddrStaistics(addr);
		addrStaistics.errorCnt++;
		Integer cnt = addrStaistics.errors.get(text);
		if (cnt == null)
			cnt = 1;
		else
			cnt = cnt + 1;
		addrStaistics.errors.put(text, cnt);
	}

	public synchronized void clear() {
		controllers.clear();
	}

	private AddrStaistics getAddrStaistics(int addr) {
		AddrStaistics addrStaistics = controllers.get(addr);
		if (addrStaistics == null) {
			addrStaistics = new AddrStaistics();
			controllers.put(addr, addrStaistics);
		}
		return addrStaistics;
	}
}
