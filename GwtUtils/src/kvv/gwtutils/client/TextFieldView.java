package kvv.gwtutils.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings("deprecation")
public abstract class TextFieldView extends Composite {

	public TextBox text = new TextBox() {
		{
			addFocusListener(new FocusListener() {
				@Override
				public void onFocus(Widget sender) {
					focused = true;
				}

				public void onLostFocus(Widget sender) {
					new Timer() {
						@Override
						public void run() {
							focused = false;
						}
					}.schedule(500);
				}
			});
		}
	};

	public Button button = new Button("Set", new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			TextFieldView.this.onClick(event);
		}
	});


	public boolean focused;

	protected abstract void onClick(ClickEvent event);

	public TextFieldView(String name, int labelWidth, int textWidth) {

		Label label = new Label(name);
		if (labelWidth != 0)
			label.setWidth(labelWidth + "px");

		text.setWidth(textWidth + "px");

		HorPanel hp = new HorPanel(true, 0, label, new Gap(4, 4), text, button);

		initWidget(hp);
	}
}
