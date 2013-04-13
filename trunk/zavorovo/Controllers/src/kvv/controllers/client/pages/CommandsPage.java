package kvv.controllers.client.pages;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CommandsPage extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public CommandsPage() {
		vertPanel.setSpacing(10);

		controllersService.getCommands(new CallbackAdapter<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				FlowPanel commandsPanel = new FlowPanel();
				for (final String cmd : result) {
					if (cmd == null) {
						vertPanel.add(commandsPanel);
						commandsPanel = new FlowPanel();
					} else {
						Button button = new Button(cmd);
						commandsPanel.add(button);
						button.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (!ModePage.controlMode) {
									Window.alert("Режим управления не включен");
									return;
								}
								controllersService.execCommand(cmd,
										new CallbackAdapter<Void>());
							}
						});
					}
				}
				vertPanel.add(commandsPanel);
			}
		});

		initWidget(vertPanel);
	}

}
