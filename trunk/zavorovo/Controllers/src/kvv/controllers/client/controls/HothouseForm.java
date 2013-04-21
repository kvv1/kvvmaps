package kvv.controllers.client.controls;

import kvv.controllers.client.controls.simple.GetRegControl;
import kvv.controllers.client.controls.simple.GetSetRegControl;
import kvv.controllers.client.controls.simple.RelayCheckBoxes;
import kvv.controllers.register.Register;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HothouseForm extends ControlComposite {

	public HothouseForm(final int addr, String name) {

		super(addr);

		GetRegControl currentTempWidget = new GetRegControl(addr,
				Register.REG_TEMP, true, "T = ");
		add(currentTempWidget);

		GetSetRegControl optTempWidget = new GetSetRegControl(addr,
				Register.REG_TEMP_PREF, true, "T опт = ");
		add(optTempWidget);

		GetSetRegControl maxTempWidget = new GetSetRegControl(addr,
				Register.REG_TEMP_PREF_2, true, "T мах = ");
		add(maxTempWidget);

		RelayCheckBoxes relays = new RelayCheckBoxes(addr);
		add(relays);

		VerticalPanel panel = new VerticalPanel();

		panel.setSpacing(10);

		panel.add(new Label(name));
		panel.add(currentTempWidget);
		panel.add(optTempWidget);
		panel.add(maxTempWidget);
		panel.add(relays);

		HorizontalPanel refreshPanel = new HorizontalPanel();

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

		refreshPanel.add(autorefresh);

		Button refreshButton = new Button("Обновить");
		refreshPanel.add(refreshButton);

		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		panel.add(refreshPanel);

		initWidget(panel);
	}
}
