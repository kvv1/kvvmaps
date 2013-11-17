package kvv.controllers.client.page;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.simple.TextWithSaveButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConfigurationPage extends Composite {
	private HorizontalPanel hpanel = new HorizontalPanel();
	private VerticalPanel vpanel = new VerticalPanel();
	private TextWithSaveButton controllers = new TextWithSaveButton(
			"Контроллеры", "400px", "600px") {
		@Override
		protected void save(String text, final AsyncCallback<Void> callback) {
			controllersService.saveControllersText(text, callback);
			// Window.Location.reload();
		}
	};

//	private TextWithSaveButton objects = new TextWithSaveButton("Формы", 400,
//			150) {
//		@Override
//		protected void save(String text, final AsyncCallback<Void> callback) {
//			controllersService.saveObjects(text, callback);
//			// Window.Location.reload();
//		}
//	};
//
//	private TextWithSaveButton registers = new TextWithSaveButton("Регистры",
//			400, 150) {
//		@Override
//		protected void save(String text, final AsyncCallback<Void> callback) {
//			controllersService.saveRegisters(text, callback);
//			// Window.Location.reload();
//		}
//	};

	private TextWithSaveButton pages = new TextWithSaveButton("Страницы", "400px",
			"600px") {
		@Override
		protected void save(String text, final AsyncCallback<Void> callback) {
			controllersService.savePagesText(text, callback);
			// Window.Location.reload();
		}
	};

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public ConfigurationPage() {

		controllersService.loadControllersText(new CallbackAdapter<String>() {
			@Override
			public void onSuccess(String result) {
				controllers.setText(result);
			}
		});

//		controllersService.loadObjects(new CallbackAdapter<String>() {
//			@Override
//			public void onSuccess(String result) {
//				objects.setText(result);
//			}
//		});
//
//		controllersService.loadRegisters(new CallbackAdapter<String>() {
//			@Override
//			public void onSuccess(String result) {
//				registers.setText(result);
//			}
//		});

		controllersService.loadPagesText(new CallbackAdapter<String>() {
			@Override
			public void onSuccess(String result) {
				if (result != null)
					pages.setText(result);
			}
		});

		hpanel.add(controllers);
//		vpanel.add(objects);
//		vpanel.add(registers);
		vpanel.add(pages);
		hpanel.add(vpanel);

		initWidget(hpanel);

	}
}
