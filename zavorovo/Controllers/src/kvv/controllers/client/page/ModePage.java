package kvv.controllers.client.page;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ModePage extends Composite {

	private VerticalPanel panel = new VerticalPanel();

	public static boolean controlMode;

	static {
		controlMode = Boolean.parseBoolean(Cookies.getCookie("ControlMode"));
	}

	public static boolean check() {
		if (!controlMode)
			Window.alert("Режим управления не включен");
		return controlMode;
	}

	public ModePage() {

		final TextBox password = new TextBox();
		panel.add(password);

		final CheckBox button = new CheckBox("Режим управления");
		panel.add(button);

		button.setValue(controlMode, controlMode);

		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!controlMode && !password.getText().equals("83217")) {
					button.setValue(false);
					Window.alert("Ошибка");
					return;
				}
				controlMode = !controlMode;
				button.setValue(controlMode, false);
				Cookies.setCookie("ControlMode", Boolean.toString(controlMode));
			}
		});

//		panel.add(new Label("" + Controllers.systemDescr.timeZoneOffset + " "
//				+ new Date().getTimezoneOffset()));

		initWidget(panel);
	}

}
