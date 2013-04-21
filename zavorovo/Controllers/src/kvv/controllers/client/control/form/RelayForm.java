package kvv.controllers.client.control.form;

import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.simple.SimpleRelayControl;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class RelayForm extends ControlComposite {

	public RelayForm(int addr, final int reg, String name) {
		super(addr);

		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setSpacing(10);

		panel.add(new Label(name));

		SimpleRelayControl cb = new SimpleRelayControl(addr, reg, "");
		add(cb);
		
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

}
