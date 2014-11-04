package kvv.controllers.client.control.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class PanelWithButton<T extends Widget> extends Composite {
	private final VerticalPanel panel = new VerticalPanel();

	public final T widget;

	protected abstract void onButton();

	public PanelWithButton(T w, String buttonText) {
		this.widget = w;
		panel.setSpacing(4);
		panel.add(w);
		panel.add(new Button(buttonText, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				onButton();
			}
		}));
		initWidget(panel);
	}
}
