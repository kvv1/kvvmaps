package kvv.controllers.client.page;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ConfigurationService;
import kvv.controllers.client.ConfigurationServiceAsync;
import kvv.controllers.client.Controllers;
import kvv.controllers.client.control.simple.TextWithSaveButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ConfigurationTabPage extends Composite {

	public ConfigurationTabPage() {
		TabPanel tabs = new TabPanel();
		tabs.setHeight("200px");

		tabs.add(new ConfigurationPageG(), "Контроллеры и страницы");
		tabs.add(new ConfigurationPage(), "Контроллеры и страницы(текст)");

		tabs.add(createControllerTypesPanel(), "Типы контроллеров");

		tabs.selectTab(0);

		initWidget(tabs);

	}

	Widget createControllerTypesPanel() {
		VerticalPanel vp = new VerticalPanel();

		HorizontalPanel buttonsPanel = new HorizontalPanel();

		final TabPanel tabs = new TabPanel();
		tabs.setHeight("200px");
		final TextBox newTabName = new TextBox();

		Button newType = new Button("Новый тип");
		newType.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String type = newTabName.getText();
				if (!type.isEmpty()) {
					tabs.add(new ControllerTypePanel(type), type);
					tabs.selectTab(tabs.getWidgetCount() - 1);
				}
			}
		});

		buttonsPanel.add(newTabName);
		buttonsPanel.add(newType);

		vp.add(buttonsPanel);

		for (String type : Controllers.systemDescr.controllerTypes.keySet()) {
			tabs.add(new ControllerTypePanel(type), type);
		}

		if (tabs.getWidgetCount() > 0)
			tabs.selectTab(0);

		vp.add(tabs);
		return vp;
	}
}

class ControllerTypePanel extends Composite {
	private final ConfigurationServiceAsync configurationService = GWT
			.create(ConfigurationService.class);

	public ControllerTypePanel(final String type) {
		HorizontalPanel hp = new HorizontalPanel();
		final TextWithSaveButton def = new TextWithSaveButton("Параметры",
				"600px", "600px") {
			@Override
			protected void save(String text, AsyncCallback<Void> callback) {
				configurationService
						.saveControllerDefText(type, text, callback);
			}
		};

		configurationService.loadControllerDefText(type,
				new CallbackAdapter<String>() {
					public void onSuccess(String result) {
						def.setText(result);
					};
				});

		hp.add(def);

		final TextWithSaveButton ui = new TextWithSaveButton("Интерфейс",
				"600px", "600px") {

			@Override
			protected void save(String text, AsyncCallback<Void> callback) {
				configurationService.saveControllerUIText(type, text, callback);
			}
		};

		configurationService.loadControllerUIText(type,
				new CallbackAdapter<String>() {
					public void onSuccess(String result) {
						ui.setText(result);
					};
				});

		hp.add(ui);

		initWidget(hp);
	}

}
