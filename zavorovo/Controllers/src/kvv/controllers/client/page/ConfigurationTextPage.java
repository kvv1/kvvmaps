package kvv.controllers.client.page;

import kvv.controllers.client.ConfigurationService;
import kvv.controllers.client.ConfigurationServiceAsync;
import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.TextWithSaveButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class ConfigurationTextPage extends Composite {
	private final ConfigurationServiceAsync configurationService = GWT
			.create(ConfigurationService.class);

	private HorizontalPanel hpanel = new HorizontalPanel();
	private TextWithSaveButton controllers = new TextWithSaveButton(
			"Контроллеры", "400px", "600px") {
		@Override
		protected void save(String text, final AsyncCallback<Void> callback) {
			configurationService.saveControllersText(text, callback);
		}
	};

	private TextWithSaveButton pages = new TextWithSaveButton("Страницы",
			"400px", "600px") {
		@Override
		protected void save(String text, final AsyncCallback<Void> callback) {
			configurationService.savePagesText(text, callback);
		}
	};

	public ConfigurationTextPage() {

		configurationService.loadControllersText(new CallbackAdapter<String>() {
			@Override
			public void onSuccess(String result) {
				controllers.setText(result);
			}
		});

		configurationService.loadPagesText(new CallbackAdapter<String>() {
			@Override
			public void onSuccess(String result) {
				if (result != null)
					pages.setText(result);
			}
		});

		hpanel.add(controllers);
		hpanel.add(pages);

		initWidget(hpanel);

	}
}
