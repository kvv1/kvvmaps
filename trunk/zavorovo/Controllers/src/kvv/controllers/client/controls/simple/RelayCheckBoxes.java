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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class RelayCheckBoxes extends Composite {

	private int addr;
	private final CheckBox checkBoxes[] = new CheckBox[4];
	private final HorizontalPanel panel = new HorizontalPanel();

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public RelayCheckBoxes(final int addr) {
		this.addr = addr;

		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		// panel.setSpacing(10);

		for (int i = 0; i < checkBoxes.length; i++) {
			final int ii = i;
			final CheckBox cb = new CheckBox();
			checkBoxes[i] = cb;
			panel.add(cb);
			cb.setEnabled(false);
			cb.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (!ModePage.controlMode) {
						cb.setValue(!((CheckBox) event.getSource()).getValue());
						Window.alert("Режим управления не включен");
						return;
					}
					cb.setEnabled(false);
					final boolean checked = ((CheckBox) event.getSource())
							.getValue();
					controllersService.setReg(addr, ii, checked ? 1 : 0,
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
		}

		initWidget(panel);
	}

	public void refresh(Map<Integer, Integer> regs) {
		for (int i = 0; i < checkBoxes.length; i++)
			checkBoxes[i].setEnabled(false);

		if (regs == null)
			return;

		Integer _val = regs.get(Register.REG_RELAYS);

		if (_val == null)
			return;

		int val = _val;

		for (int i = 0; i < checkBoxes.length; i++) {
			checkBoxes[i].setValue((val & 1) != 0);
			checkBoxes[i].setEnabled(true);
			val >>= 1;
		}

	}

	public void refresh() {
		for (int i = 0; i < checkBoxes.length; i++)
			checkBoxes[i].setEnabled(false);

		controllersService.getReg(addr, Register.REG_RELAYS,
				new AsyncCallback<Integer>() {
					@Override
					public void onSuccess(Integer result) {
						int n = result;
						for (int i = 0; i < checkBoxes.length; i++) {
							checkBoxes[i].setValue((n & 1) != 0);
							checkBoxes[i].setEnabled(true);
							n >>= 1;
						}
					}

					@Override
					public void onFailure(Throwable caught) {
					}
				});
	}
}
