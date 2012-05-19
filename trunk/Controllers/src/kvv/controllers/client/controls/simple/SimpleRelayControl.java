package kvv.controllers.client.controls.simple;

import java.util.Map;

import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.pages.ModePage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;

public class SimpleRelayControl extends Composite {

	private final CheckBox cb = new CheckBox();

	private final int addr;
	private final int reg;
	private final ControllersServiceAsync controllersService;

	public SimpleRelayControl(final int addr, final int reg,
			final ControllersServiceAsync controllersService) {

		this.addr = addr;
		this.reg = reg;
		this.controllersService = controllersService;

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

	public void refresh() {
		cb.setEnabled(false);

		controllersService.getReg(addr, reg, new AsyncCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				cb.setEnabled(true);
				cb.setValue(result != 0);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	public void refresh(Map<Integer, Integer> result) {
		if (result == null) {
			cb.setEnabled(false);
		} else {
			cb.setEnabled(true);
			cb.setValue(result.get(reg) != 0);
		}
	}
}
