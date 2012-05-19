package kvv.controllers.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ControllersMap {
	private final HashMap<Integer, Map<Integer, Integer>> map = new HashMap<Integer, Map<Integer, Integer>>();

	private final ControllersServiceAsync controllersService;

	public ControllersMap(ControllersServiceAsync controllersService) {
		this.controllersService = controllersService;
	}

	public Integer get(int addr, int reg) {
		Map<Integer, Integer> regs = map.get(addr);
		if (regs == null) {
			try {
				controllersService.getRegs(addr, new AsyncCallback<Map<Integer,Integer>>() {
					@Override
					public void onSuccess(Map<Integer, Integer> result) {
					}
					
					@Override
					public void onFailure(Throwable caught) {
					}
				});
			} catch (Exception e) {
				return null;
			}
			map.put(addr, regs);
		}
		return regs.get(reg);
	}
}
