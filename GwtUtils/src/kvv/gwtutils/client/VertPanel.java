package kvv.gwtutils.client;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class VertPanel extends VerticalPanel {
	public VertPanel(Widget... widgets) {
		for (Widget w : widgets)
			add(w);
	}

	public VertPanel(
			HasHorizontalAlignment.HorizontalAlignmentConstant alignmentConstant,
			Widget... widgets) {
		setHorizontalAlignment(alignmentConstant);
		for (Widget w : widgets)
			add(w);
	}
}
