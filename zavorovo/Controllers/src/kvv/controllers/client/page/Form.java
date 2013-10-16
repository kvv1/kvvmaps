package kvv.controllers.client.page;

import java.util.Set;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ChildComposite;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.simple.GetRegControl;
import kvv.controllers.client.control.simple.GetSetRegControl;
import kvv.controllers.client.control.simple.SimpleRelayControl;
import kvv.controllers.register.AllRegs;
import kvv.controllers.register.Register;
import kvv.controllers.register.RegisterUI;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

class Form extends ControlComposite {

	private final VerticalPanel panel = new VerticalPanel();

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private final int addr;
	private final boolean addRefreshButton;
	private Label nameLabel;

	public Form(int addr, String name, boolean addRefreshButton) {
		this.addr = addr;
		this.addRefreshButton = addRefreshButton;
		nameLabel = new Label(name + "(" + addr + ")");
		initWidget(panel);
		refreshUI();
	}

	public void refreshUI(AllRegs result) {
		removeChildren();
		panel.clear();

		panel.add(nameLabel);

		if (result == null)
			return;

		Grid grid = new Grid(result.ui.size(), 2);
		panel.add(grid);

		if (addRefreshButton) {
			Button refresh = new Button("Обновить");
			panel.add(refresh);

			refresh.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					refresh();
				}
			});
		}

		int row = 0;
		for (RegisterUI reg : result.ui) {
			switch (reg.type) {
			case checkbox:
				SimpleRelayControl cb = new SimpleRelayControl(addr, reg.reg,
						null);
				grid.setWidget(row, 0, new Label(reg.text));
				grid.setWidget(row, 1, cb);
				add(cb);
				break;
			case textRO: {
				ChildComposite control;
				control = new GetRegControl(addr, reg.reg, 1, null);
				grid.setWidget(row, 0, new Label(reg.text));
				grid.setWidget(row, 1, control);
				add(control);
				break;
			}
			case textRW: {
				ChildComposite control;
				control = new GetSetRegControl(addr, reg.reg, false, "");
				grid.setWidget(row, 0, new Label(reg.text));
				grid.setWidget(row, 1, control);
				add(control);
				break;
			}
			default:
				break;
			}
			row++;
		}

		refresh(result);
	}

	@Override
	public void refresh(AllRegs result) {

		super.refresh(result);
		nameLabel.removeStyleName("stoppedVM");
		nameLabel.removeStyleName("disabled");
		if (result == null || result.values.get(Register.REG_VMONOFF) == null)
			nameLabel.addStyleName("disabled");
		else if (result.values.get(Register.REG_VMONOFF) == 0)
			nameLabel.addStyleName("stoppedVM");
	}

	public void refreshUI() {
		refreshUI(null);

		controllersService.getRegs(addr, new AsyncCallback<AllRegs>() {

			@Override
			public void onSuccess(AllRegs result) {
				refreshUI(result);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	@Override
	public Set<Integer> getAddrs() {
		Set<Integer> addrs = super.getAddrs();
		addrs.add(addr);
		return addrs;
	}

}