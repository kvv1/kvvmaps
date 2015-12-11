package kvv.controllers.client;

import kvv.gwtutils.client.VertPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;

public class LogTabPage extends Composite {

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	
	public LogTabPage() {
		final TextArea text = new TextArea();
		text.setSize("1000px", "700px");
		Button update = new Button("Update", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controllersService.getModbusLog(new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						text.setText(result);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						text.setText(caught.getMessage());
					}
				});
			}
		});
		
		VertPanel vp = new VertPanel(update, text);
		initWidget(vp);
	}
}
