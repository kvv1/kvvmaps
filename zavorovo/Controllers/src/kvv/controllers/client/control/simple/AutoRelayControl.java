package kvv.controllers.client.control.simple;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.register.AllRegs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;

public class AutoRelayControl extends ControlComposite {

	private final CheckBox cb;

	private final int reg;
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public AutoRelayControl(final int addr, final int reg, String label) {
		super(addr);

		cb = new CheckBox(label);
		this.reg = reg;

		cb.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ModePage.controlMode) {
					cb.setValue(!((CheckBox) event.getSource()).getValue());
					Window.alert("Режим управления не включен");
					return;
				}
				final boolean checked = ((CheckBox) event.getSource())
						.getValue();
				cb.setEnabled(false);
				controllersService.setReg(addr, reg, checked ? 1 : 0,
						new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								cb.setEnabled(true);
								cb.setValue(checked);
							}

							@Override
							public void onFailure(Throwable caught) {
							}
						});
			}
		});

		cb.setEnabled(false);
		initWidget(cb);
	}

	@Override
	public void refresh(AllRegs result) {
		if (result == null) {
			cb.setEnabled(false);
		} else {
			cb.setEnabled(true);
			cb.setValue(result.values.get(reg) != 0);
		}
	}
}
