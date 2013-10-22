package kvv.controllers.client.page;

import java.util.HashSet;
import java.util.Set;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.form.MU110_8Form;
import kvv.controllers.client.control.form.Type2Form;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.PageDescr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ControllersPage extends Composite {

	private VerticalPanel vertPanel = new VerticalPanel();
	private final Set<ControlComposite> objects = new HashSet<ControlComposite>();

	private static final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public static ControllerDescr[] controllers;
	public static PageDescr[] pages;

	public static void loadData(final AsyncCallback<Void> callback) {
		controllersService
				.getControllers(new CallbackAdapter<ControllerDescr[]>() {
					@Override
					public void onSuccess(ControllerDescr[] result) {
						controllers = result;
						controllersService
								.getPages(new AsyncCallback<PageDescr[]>() {
									@Override
									public void onSuccess(PageDescr[] result) {
										pages = result;
										callback.onSuccess(null);
									}

									@Override
									public void onFailure(Throwable caught) {
										callback.onFailure(caught);
									}
								});
					}

					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
	}

	public ControllersPage() {

		// vertPanel.setSpacing(10);

		Button refreshButton = new Button("Обновить");
		vertPanel.add(refreshButton);
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				for (ControlComposite c : objects)
					c.refresh();
			}
		});

		for (ControllerDescr descr : controllers) {
			if (descr != null && descr.addr != 0) {
				switch (descr.type) {
				case TYPE2: {
					ControlComposite control = new Type2Form(descr.addr,
							descr.name);
					objects.add(control);
					vertPanel.add(control);
					break;
				}
				case MU110_8: {
					ControlComposite control = new MU110_8Form(descr.addr,
							descr.name);
					objects.add(control);
					vertPanel.add(control);
					break;
				}
				}
			}
		}

		initWidget(vertPanel);
	}

}
