package kvv.controllers.client.controls.simple;

import java.util.Map;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.pages.ModePage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class GetSetRegControl extends Composite {

	private final HorizontalPanel panel = new HorizontalPanel();

	private final int addr;
	private final Label label;
	private final TextBox edit;
	private final int reg;
	private final boolean div10;
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public GetSetRegControl(final int addr, final int reg, final boolean div10,
			String text) {
		this.addr = addr;
		this.label = new Label(text);
		this.edit = new TextBox();
		this.reg = reg;
		this.div10 = div10;

		edit.setWidth("40px");

		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		panel.add(label);
		panel.add(edit);

		edit.setEnabled(false);
		edit.setText("???");

		Button setButton = new Button("Set");
		setButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ModePage.controlMode) {
					Window.alert("Режим управления не включен");
					return;
				}
				edit.setEnabled(false);
				try {
					int val = div10 ? Integer.valueOf(edit.getText()) * 10
							: Integer.valueOf(edit.getText());
					controllersService.setReg(addr, reg, val,
							new AsyncCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									edit.setEnabled(true);
								}

								@Override
								public void onFailure(Throwable caught) {
									edit.setText("---");
								}
							});
				} catch (NumberFormatException e) {
				}
			}
		});
		panel.add(setButton);

		initWidget(panel);
	}

	public void refresh(Map<Integer, Integer> regs) {
		edit.setEnabled(false);
		edit.setText("???");

		if (regs == null)
			return;

		Integer _val = regs.get(reg);

		if (_val == null)
			return;

		if (div10)
			edit.setText(Float.toString((float) _val / 10));
		else
			edit.setText(Integer.toString(_val));
		edit.setEnabled(true);
	}

	public void refresh() {
		edit.setEnabled(false);
		edit.setText("???");
		controllersService.getReg(addr, reg, new AsyncCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				if (div10)
					edit.setText(Float.toString((float) result / 10));
				else
					edit.setText(Integer.toString(result));
				edit.setEnabled(true);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

}
