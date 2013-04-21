package kvv.controllers.client.control.vm;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SourceEditor extends Composite{

	HorizontalPanel buttons = new HorizontalPanel();
	VerticalPanel panel = new VerticalPanel();
	
	private final TextArea text = new TextArea();

	public SourceEditor(int addr) {

		Button ok = new Button("OK");
		Button cancel = new Button("Cancel");

		ok.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
			}
		});
		
		buttons.add(ok);
		buttons.add(cancel);
		
		panel.add(text);
		panel.add(buttons);
		
		initWidget(panel);
	}
}
