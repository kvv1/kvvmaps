package kvv.controllers.client.control.simple;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.register.AllRegs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class GetSetRegControl extends ControlComposite {

	private final HorizontalPanel panel = new HorizontalPanel();

	private final Label label;
	private final TextBox edit;
	private final int reg;
	private final boolean div10;
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public GetSetRegControl(final int addr, final int reg, final boolean div10,
			String text) {
		super(addr);
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
				if (!ModePage.check())
					return;

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
								}
							});
				} catch (NumberFormatException e) {
					refresh();
				}
			}
		});
		panel.add(setButton);

		initWidget(panel);
	}

	@Override
	public void refresh(AllRegs regs) {
		edit.setEnabled(false);
		edit.setText("???");

		if (regs == null)
			return;

		Integer _val = regs.values.get(reg);

		if (_val == null)
			return;

		if (div10)
			edit.setText(Float.toString((float) _val / 10));
		else
			edit.setText(Integer.toString(_val));
		edit.setEnabled(true);
	}
}
