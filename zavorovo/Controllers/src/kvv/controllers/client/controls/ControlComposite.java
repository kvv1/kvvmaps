package kvv.controllers.client.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

public class ControlComposite extends Composite {

	public final int addr;

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private Collection<ControlComposite> children = new ArrayList<ControlComposite>();

	public ControlComposite(int addr) {
		this.addr = addr;
	}

	public void add(ControlComposite child) {
		children.add(child);
	}

	public void refresh(Map<Integer, Integer> result) {
		for (ControlComposite controlComposite : children)
			controlComposite.refresh(result);
	}

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
