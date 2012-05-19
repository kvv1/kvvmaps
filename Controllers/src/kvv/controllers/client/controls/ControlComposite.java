package kvv.controllers.client.controls;

import java.util.Map;

import kvv.controllers.client.ControllersServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

public abstract class ControlComposite extends Composite {
	public final int addr;
	private final ControllersServiceAsync controllersService;

	public ControlComposite(int addr, ControllersServiceAsync controllersService) {
		this.addr = addr;
		this.controllersService = controllersService;
	}

	public abstract void refresh(Map<Integer, Integer> result);

	public void refresh() {
		refresh(null);
		controllersService.getRegs(addr,
				new AsyncCallback<Map<Integer, Integer>>() {

					@Override
					public void onSuccess(Map<Integer, Integer> result) {
						refresh(result);
					}

					@Override
					public void onFailure(Throwable caught) {
					}
				});
	}

}
