package kvv.controllers.client.control;

import java.util.HashSet;
import java.util.Set;

import kvv.controller.register.AllRegs;
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
		for (Integer addr : addrs)
			controllersService.getRegs(addr, new AsyncCallback<AllRegs>() {

				@Override
				public void onSuccess(AllRegs result) {
					refresh(result);
				}

				@Override
				public void onFailure(Throwable caught) {
				}
			});
	}

	public void setEnabled(boolean b) {
	}
}
