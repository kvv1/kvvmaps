package kvv.controllers.client.page;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class ControllersDescrPage extends Composite {
	private HorizontalPanel panel = new HorizontalPanel();
	private TextWithSaveButton controllers = new TextWithSaveButton() {
		@Override
		protected void save(String text, final AsyncCallback<Void> callback) {
			controllersService.save(text, callback);
		}
	};

	private TextWithSaveButton objects = new TextWithSaveButton() {
		@Override
		protected void save(String text, final AsyncCallback<Void> callback) {
		}
	};

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public ControllersDescrPage() {

		controllersService.load(new CallbackAdapter<String>() {
			@Override
			public void onSuccess(String result) {
				controllers.setText(result);
			}
		});

		controllersService.loadObjects(new CallbackAdapter<String>() {
			@Override
			public void onSuccess(String result) {
				objects.setText(result);
			}
		});

		panel.add(controllers);
		panel.add(objects);

		initWidget(panel);

	}
}
