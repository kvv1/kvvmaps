package kvv.controllers.client.pages;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ModePage extends Composite {

	private VerticalPanel panel = new VerticalPanel();

	public static boolean controlMode;

	public ModePage() {

		final TextBox password = new TextBox();
		panel.add(password);

		final CheckBox button = new CheckBox("Режим управления");
		panel.add(button);

		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!controlMode && !password.getText().equals("83217")) {
					button.setValue(!((CheckBox) event.getSource()).getValue());
					Window.alert("Ошибка");
					return;
				}
				controlMode = !controlMode;
			}
		});

		initWidget(panel);
	}

}
