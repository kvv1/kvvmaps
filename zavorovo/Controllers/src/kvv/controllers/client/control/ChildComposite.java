package kvv.controllers.client.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kvv.controllers.client.Controllers;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

public abstract class ChildComposite extends Composite implements ControlChild {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private final Set<Integer> addrs = new HashSet<Integer>();

	public ChildComposite(int addr) {
		this.addrs.add(addr);
	}

	@Override
	public final Set<Integer> getAddrs() {
		return addrs;
	}

	public void refresh() {
		refresh(null);
		for (final Integer addr : addrs)
			controllersService.getRegs(addr,
					new AsyncCallback<HashMap<Integer, Integer>>() {
						@Override
						public void onSuccess(HashMap<Integer, Integer> result) {
							Controllers.adjust(addr, result);
							refresh(new AllRegs(addr, result));
						}

						@Override
						public void onFailure(Throwable caught) {
						}
					});
	}

	public void setEnabled(boolean b) {
	}
}
