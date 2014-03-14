package kvv.controllers.client.control.simple;

import kvv.controllers.client.control.ControlComposite;

import com.google.gwt.user.client.ui.Grid;

public class RelayCheckBoxes extends ControlComposite {

	public RelayCheckBoxes(final int addr, int reg0, Integer pwm0, int n) {
		Grid panel = new Grid(pwm0 == null ? 1 : 2, n);

		for (int i = 0; i < n; i++) {
			SimpleRelayControl checkBox = new SimpleRelayControl(addr,
					reg0 + i, null);
			add(checkBox);
			panel.setWidget(0, i, checkBox);

			if (pwm0 != null) {
				GetSetRegControl2 pwm = new GetSetRegControl2(addr, pwm0 + i,
						null);
				add(pwm);
				panel.setWidget(1, i, pwm);
			}
		}

		initWidget(panel);
	}
}
