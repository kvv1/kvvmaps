package kvv.controllers.client.control.form;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HorPanel extends HorizontalPanel {
	public HorPanel(Widget... widgets) {
		for (Widget w : widgets)
			add(w);
	}
}
