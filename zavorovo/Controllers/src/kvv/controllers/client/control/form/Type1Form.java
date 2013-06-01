package kvv.controllers.client.control.form;

import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.simple.GetRegControl;
import kvv.controllers.client.control.simple.GetSetRegControl;
import kvv.controllers.client.control.simple.RelayCheckBoxes;
import kvv.controllers.client.control.simple.SimpleRelayControl;
import kvv.controllers.register.Register;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class Type1Form extends ControlComposite {

	public Type1Form(int addr, String name) {

		super(addr);

		HorizontalPanel panel = new HorizontalPanel();

		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setSpacing(10);

		Label nameLabel = new Label(name);
		nameLabel.setWidth("100px");
		panel.add(nameLabel);

		Label addrLabel = new Label("addr = " + addr);
		addrLabel.setWidth("70px");
		panel.add(addrLabel);

		RelayCheckBoxes relays = new RelayCheckBoxes(addr, Register.REG_RELAY0, Register.REG_RELAY_CNT);
		add(relays);
		panel.add(relays);

		GetRegControl tempVal = new GetRegControl(addr, Register.REG_TEMP,
				true, "T=");
		add(tempVal);
		panel.add(tempVal);

		GetSetRegControl tempPref = new GetSetRegControl(addr,
				Register.REG_TEMP_PREF, true, "T опт=");
		add(tempPref);
		panel.add(tempPref);

		GetSetRegControl tempMax = new GetSetRegControl(addr,
				Register.REG_TEMP_PREF_2, true, "T макс=");
		add(tempMax);
		panel.add(tempMax);

		SimpleRelayControl hothouseCheckBox = new SimpleRelayControl(addr,
				Register.REG_TEMP_PREF_ON, "Теплица");
		add(hothouseCheckBox);
		panel.add(hothouseCheckBox);

		Button refreshButton = new Button("Обновить");
		panel.add(refreshButton);
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		final Timer refreshTimer = new Timer() {
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
					refreshTimer.schedule(100);
				else
					refreshTimer.cancel();
			}
		});

		panel.add(autorefresh);

		initWidget(panel);
		refresh();
	}

}
