package kvv.controllers.client.controls.simple;

import kvv.controllers.client.controls.ControlComposite;
import kvv.controllers.register.Register;

import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class RelayCheckBoxes extends ControlComposite {

	public RelayCheckBoxes(final int addr) {
		super(addr);

		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		// panel.setSpacing(10);

		for (int i = 0; i < 4; i++) {
			SimpleRelayControl checkBox = new SimpleRelayControl(addr,
					Register.REG_RELAY0 + i, null);
			add(checkBox);
			panel.add(checkBox);
		}

		initWidget(panel);
	}
}
