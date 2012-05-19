package kvv.controllers.client.controls;

import java.util.Map;

import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.controls.simple.GetRegControl;
import kvv.controllers.client.controls.simple.GetSetRegControl;
import kvv.controllers.client.controls.simple.RelayCheckBoxes;
import kvv.controllers.shared.Constants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HothouseControl extends ControlComposite {

	private final VerticalPanel panel = new VerticalPanel();

	private final GetRegControl currentTempWidget;
	private final GetSetRegControl optTempWidget;
	private final GetSetRegControl maxTempWidget;
	private final RelayCheckBoxes relays;

	public HothouseControl(final int addr, String name,
			ControllersServiceAsync controllersService) {

		super(addr, controllersService);

		currentTempWidget = new GetRegControl(addr, Constants.REG_TEMP, true,
				"T = ", controllersService);
		optTempWidget = new GetSetRegControl(addr, Constants.REG_TEMP_PREF,
				true, "T опт = ", controllersService);
		maxTempWidget = new GetSetRegControl(addr, Constants.REG_TEMP_PREF_2,
				true, "T мах = ", controllersService);
		relays = new RelayCheckBoxes(addr, controllersService);

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

	public void refresh(Map<Integer, Integer> regs) {
		relays.refresh(regs);
		currentTempWidget.refresh(regs);
		optTempWidget.refresh(regs);
		maxTempWidget.refresh(regs);
	}

}
