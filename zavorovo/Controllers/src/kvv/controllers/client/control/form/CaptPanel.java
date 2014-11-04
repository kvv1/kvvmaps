package kvv.controllers.client.control.form;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Widget;

public class CaptPanel extends CaptionPanel {

	public CaptPanel(String title, Widget widget) {
		super(title);
		add(widget);
	}

}
