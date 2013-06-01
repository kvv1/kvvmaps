package kvv.controllers.client.control.simple;

import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.register.Register;

import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class RelayCheckBoxes extends ControlComposite {

	public RelayCheckBoxes(final int addr, int reg0, int n) {
		super(addr);

		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		// panel.setSpacing(10);

		for (int i = 0; i < n; i++) {
			SimpleRelayControl checkBox = new SimpleRelayControl(addr,
					reg0 + i, null);
			add(checkBox);
			panel.add(checkBox);
		}

		initWidget(panel);
	}
}
