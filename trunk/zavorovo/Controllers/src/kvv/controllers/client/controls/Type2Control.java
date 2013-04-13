package kvv.controllers.client.controls;

import java.util.Map;

import kvv.controllers.client.controls.simple.GetRegControl;
import kvv.controllers.client.controls.simple.RelayCheckBoxes;
import kvv.controllers.client.controls.vm.VMControl;
import kvv.controllers.register.Register;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class Type2Control extends ControlComposite {

	private final HorizontalPanel panel = new HorizontalPanel();

	private final GetRegControl tempVal;
	private final RelayCheckBoxes relays;
	private final VMControl vmControl;

	public Type2Control(int addr, String name) {

		super(addr);

		relays = new RelayCheckBoxes(addr);

		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setSpacing(10);

		Label nameLabel = new Label(name);
		nameLabel.setWidth("100px");

		panel.add(nameLabel);

		Label addrLabel = new Label("addr = " + addr);
		addrLabel.setWidth("70px");

		panel.add(addrLabel);

		panel.add(relays);

		tempVal = new GetRegControl(addr, Register.REG_TEMP, true, "T=");
		panel.add(tempVal);

		vmControl = new VMControl(addr, name);
		panel.add(vmControl);

		Button refreshButton = new Button("Обновить");
		panel.add(refreshButton);

		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		final Timer removeDelay = new Timer() {
			public void run() {
				refresh();
				schedule(5000);
			}
		};

		final CheckBox autorefresh = new CheckBox("Автообновление");
		autorefresh.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (autorefresh.getValue())
					removeDelay.schedule(100);
				else
					removeDelay.cancel();
			}
		});

		panel.add(autorefresh);

		initWidget(panel);
		refresh();
	}

	@Override
	public void refresh(Map<Integer, Integer> result) {
		relays.refresh(result);
		tempVal.refresh(result);
		vmControl.refresh(result);
	}
}
