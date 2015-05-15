package kvv.heliostat.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class Button1 extends Button {
	public Button1(String string, int width, ClickHandler clickHandler) {
		super(string, clickHandler);
		setWidth(width + "px");
	}
}