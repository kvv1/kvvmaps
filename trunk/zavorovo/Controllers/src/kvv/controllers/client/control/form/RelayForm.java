package kvv.controllers.client.control.form;

import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.simple.SimpleRelayControl;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RelayForm extends ControlComposite {

	public RelayForm(int addr, final int reg, String name) {
		super(addr);

		VerticalPanel verticalPanel = new VerticalPanel();

		verticalPanel.add(new Label(name));

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.setSpacing(10);

		VerticalPanel panel = new VerticalPanel();
		panel.add(new RadioButton("group", "Авто"));
		HorizontalPanel panel1 = new HorizontalPanel();
		panel1.add(new RadioButton("group", "Ручн."));
		SimpleRelayControl cb = new SimpleRelayControl(addr, reg, "");
		add(cb);
		panel1.add(cb);
		panel.add(panel1);

		horizontalPanel.add(panel);

		verticalPanel.add(horizontalPanel);

		Button refreshButton = new Button("Обновить");
		verticalPanel.add(refreshButton);

		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		initWidget(verticalPanel);
	}

}
