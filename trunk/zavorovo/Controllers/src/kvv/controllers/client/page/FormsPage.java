package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Collection;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.shared.ControllerDescr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FormsPage extends Composite {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

//	private final SourcesServiceAsync sourcesService = GWT
//			.create(SourcesService.class);

	private final VerticalPanel vertPanel = new VerticalPanel();
	private final VerticalPanel formsPanel = new VerticalPanel();
	private final Collection<Form> objects = new ArrayList<Form>();

	public FormsPage() {
		formsPanel.setSpacing(10);
		formsPanel.setBorderWidth(1);

		Button refreshButton = new Button("Обновить");
		vertPanel.add(refreshButton);
		vertPanel.add(formsPanel);

		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				for (Form form : objects)
					form.refreshUI();
			}
		});

		initWidget(vertPanel);

		createForms();
		for (Form form : objects)
			form.refreshUI();
	}

	private void createForms() {
		controllersService
				.getControllers(new CallbackAdapter<ControllerDescr[]>() {
					@Override
					public void onSuccess(ControllerDescr[] result) {
						for (final ControllerDescr controllerDescr : result) {
							if (controllerDescr != null && controllerDescr.ui) {
								Form form = new Form(controllerDescr.addr,
										controllerDescr.name);
								formsPanel.add(form);
								objects.add(form);
								form.refresh();
							}
						}

					}
				});
	}

}
