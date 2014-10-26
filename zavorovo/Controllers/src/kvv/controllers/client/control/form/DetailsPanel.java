package kvv.controllers.client.control.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DetailsPanel extends VerticalPanel {
	public final Button b = new Button("?");

	public DetailsPanel(final String label, final Widget widget) {
		b.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				widget.setVisible(!widget.isVisible());
				b.setText(widget.isVisible() ? "Скрыть" : label);
			}
		});

		add(b);
		add(widget);
		widget.setVisible(false);
		b.setText(widget.isVisible() ? "Скрыть" : label);
	}
}
