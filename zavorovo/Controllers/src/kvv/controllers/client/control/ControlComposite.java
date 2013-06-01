package kvv.controllers.client.control;

import java.util.ArrayList;
import java.util.Collection;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.register.AllRegs;

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

	public void removeChildren() {
		children.clear();
	}
	
	public void refresh(AllRegs result) {
		for (ControlComposite controlComposite : children)
			controlComposite.refresh(result);
	}

	public void refresh() {
		refresh(null);
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

}
