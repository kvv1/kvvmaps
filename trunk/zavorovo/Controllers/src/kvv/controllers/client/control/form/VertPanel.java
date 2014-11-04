package kvv.controllers.client.control.form;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class VertPanel extends VerticalPanel {
	public VertPanel(Widget... widgets) {
		for (Widget w : widgets)
			add(w);
	}
}
