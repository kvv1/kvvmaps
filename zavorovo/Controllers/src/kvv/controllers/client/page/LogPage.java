package kvv.controllers.client.page;

import kvv.controllers.client.LogService;
import kvv.controllers.client.LogServiceAsync;
import kvv.gwtutils.client.CallbackAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LogPage extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private TextArea text = new TextArea();
	private Button update = new Button("Обновить");

	private final LogServiceAsync logService = GWT.create(LogService.class);

	public LogPage() {
		text.setSize("800px", "600px");

		vertPanel.add(text);
		vertPanel.add(update);

		update.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				update();
			}
		});

		initWidget(vertPanel);
	}

	private void update() {
		text.setText("loading...");
		logService.getLog(new CallbackAdapter<String>() {
			@Override
			public void onSuccess(String result) {
				text.setText(result);
			}
		});

	}
}
