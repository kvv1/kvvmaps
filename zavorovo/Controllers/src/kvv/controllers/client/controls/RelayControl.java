package kvv.controllers.client.controls;

import java.util.Map;

import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.controls.simple.SimpleRelayControl;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class RelayControl extends ControlComposite {

	private final HorizontalPanel panel = new HorizontalPanel();
	private SimpleRelayControl cb;

	public RelayControl(int addr, final int reg, String name,
			ControllersServiceAsync controllersService) {
		super(addr, controllersService);

		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setSpacing(10);

		panel.add(new Label(name));

		cb = new SimpleRelayControl(addr, reg, controllersService);
		panel.add(cb);

		Button refreshButton = new Button("Обновить");
		panel.add(refreshButton);

		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		initWidget(panel);
	}

	@Override
	public void refresh() {
		cb.refresh();
	}

	@Override
	public void refresh(Map<Integer, Integer> result) {
		cb.refresh(result);
	}

}
