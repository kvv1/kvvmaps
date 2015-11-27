package kvv.controllers.client.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kvv.controllers.client.Controllers;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

public class ControlComposite extends Composite implements ControlChild {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	protected Collection<ControlChild> children = new ArrayList<ControlChild>();

	public void add(ControlChild child) {
		children.add(child);
	}

	public void removeChildren() {
		children.clear();
	}

	public void refresh() {
		Set<Integer> addrs = new HashSet<Integer>();
		for (ControlChild controlComposite : children) {
			controlComposite.refresh(null);
			addrs.addAll(controlComposite.getAddrs());
		}
		for (final Integer addr : addrs) {
			controllersService.getRegs(addr,
					new AsyncCallback<HashMap<Integer, Integer>>() {

						@Override
						public void onSuccess(HashMap<Integer, Integer> result) {
							Controllers.adjust(addr, result);
							for (ControlChild controlComposite : children) {
								if (controlComposite.getAddrs().contains(addr))
									controlComposite.refresh(new AllRegs(addr,
											result));
							}
						}

						@Override
						public void onFailure(Throwable caught) {
						}
					});

		}

	}

	@Override
	public Set<Integer> getAddrs() {
		Set<Integer> res = new HashSet<Integer>();
		for (ControlChild child : children)
			res.addAll(child.getAddrs());
		return res;
	}

	@Override
	public void refresh(AllRegs result) {
		for (ControlChild controlComposite : children) {
			if (result == null
					|| controlComposite.getAddrs().contains(result.addr))
				try {
					controlComposite.refresh(result);
				} catch (Exception e) {
				}
		}
	}

}
