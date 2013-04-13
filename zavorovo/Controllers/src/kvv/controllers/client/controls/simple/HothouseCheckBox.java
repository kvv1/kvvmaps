package kvv.controllers.client.controls.simple;

import java.util.Map;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.pages.ModePage;
import kvv.controllers.register.Register;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;

public class HothouseCheckBox extends Composite {

	private final CheckBox tempRegCheckBox;

	private final int addr;
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public HothouseCheckBox(final int addr, String text) {
		this.addr = addr;
		this.tempRegCheckBox = new CheckBox(text);
		tempRegCheckBox.setEnabled(false);

		tempRegCheckBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ModePage.controlMode) {
					tempRegCheckBox.setValue(!((CheckBox) event.getSource())
							.getValue());
					Window.alert("Режим управления не включен");
					return;
				}
				tempRegCheckBox.setEnabled(false);
				final boolean checked = ((CheckBox) event.getSource())
						.getValue();
				controllersService.setReg(addr, Register.REG_TEMP_PREF_ON,
						checked ? 1 : 0, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								tempRegCheckBox.setEnabled(true);
								tempRegCheckBox.setValue(checked);
							}

							@Override
							public void onFailure(Throwable caught) {
							}
						});
			}
		});
		initWidget(tempRegCheckBox);
	}

	public void refresh(Map<Integer, Integer> regs) {
		tempRegCheckBox.setEnabled(false);

		if (regs == null)
			return;

		Integer _val = regs.get(Register.REG_TEMP_PREF_ON);

		if (_val == null)
			return;

		tempRegCheckBox.setValue(_val != 0);
		tempRegCheckBox.setEnabled(true);
	}

	public void refresh() {
		tempRegCheckBox.setEnabled(false);
		controllersService.getReg(addr, Register.REG_TEMP_PREF_ON,
				new AsyncCallback<Integer>() {
					@Override
					public void onSuccess(Integer result) {
						int n = result;
						tempRegCheckBox.setEnabled(true);
						tempRegCheckBox.setValue((n & 1) != 0);
					}

					@Override
					public void onFailure(Throwable caught) {
					}
				});
	}
}
