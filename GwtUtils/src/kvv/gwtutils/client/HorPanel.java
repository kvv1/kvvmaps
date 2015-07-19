package kvv.gwtutils.client;

import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HorPanel extends HorizontalPanel {
	public HorPanel(Widget... widgets) {
		for (Widget w : widgets)
			add(w);
	}

	public HorPanel(boolean center, int gap, Widget... widgets) {
		if (center)
			setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		for (Widget w : widgets) {
			if (w != widgets[0] && gap != 0)
				add(new Gap(gap, 1));
			add(w);
		}
	}

	public HorPanel(
			HasVerticalAlignment.VerticalAlignmentConstant verticalAlignment,
			Widget... widgets) {
		setVerticalAlignment(verticalAlignment);
		for (Widget w : widgets)
			add(w);
	}

	public void add(Widget... widgets) {
		for (Widget w : widgets)
			add(w);
	}
}
