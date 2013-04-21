package kvv.controllers.client.control.simple;

import java.util.Map;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.pages.ModePage;
import kvv.controllers.register.Register;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;

public class SimpleRelayControl extends ControlComposite {

	private final CheckBox cb;

	private final int regForRefresh;
	private final int bit;
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public SimpleRelayControl(final int addr, final int reg, String label) {
		super(addr);

		cb = new CheckBox(label);

		if (reg >= Register.REG_RELAY0
				&& reg < Register.REG_RELAY0 + Register.REG_RELAY_CNT) {
			regForRefresh = Register.REG_RELAYS;
			bit = reg - Register.REG_RELAY0;
		} else {
			regForRefresh = reg;
			bit = 0;
		}

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

	public void refresh(Map<Integer, Integer> result) {
		if (result == null) {
			cb.setEnabled(false);
		} else {
			cb.setEnabled(true);
			cb.setValue(((result.get(regForRefresh) >> bit) & 1) != 0);
		}
	}
}
