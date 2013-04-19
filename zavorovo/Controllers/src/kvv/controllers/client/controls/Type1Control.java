package kvv.controllers.client.controls;

import java.util.Map;

import kvv.controllers.client.controls.simple.GetRegControl;
import kvv.controllers.client.controls.simple.GetSetRegControl;
import kvv.controllers.client.controls.simple.HothouseCheckBox;
import kvv.controllers.client.controls.simple.RelayCheckBoxes;
import kvv.controllers.register.Register;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class Type1Control extends ControlComposite {

	private final HorizontalPanel panel = new HorizontalPanel();

	private final GetRegControl tempVal;
	private final GetSetRegControl tempPref;
	private final GetSetRegControl tempMax;

	private final RelayCheckBoxes relays;
	private final HothouseCheckBox hothouseCheckBox;

	public Type1Control(int addr, String name) {

		super(addr);

		relays = new RelayCheckBoxes(addr);

		hothouseCheckBox = new HothouseCheckBox(addr, "Теплица");

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

		tempPref = new GetSetRegControl(addr, Register.REG_TEMP_PREF, true,
				"T опт=");
		panel.add(tempPref);

		tempMax = new GetSetRegControl(addr, Register.REG_TEMP_PREF_2, true,
				"T макс=");
		panel.add(tempMax);

		panel.add(hothouseCheckBox);

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
		hothouseCheckBox.refresh(result);
		tempVal.refresh(result);
		tempPref.refresh(result);
		tempMax.refresh(result);
	}
}
