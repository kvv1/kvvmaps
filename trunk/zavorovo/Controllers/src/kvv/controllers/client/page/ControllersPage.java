package kvv.controllers.client.page;

import java.util.HashSet;
import java.util.Set;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.form.Type1Form;
import kvv.controllers.client.control.form.Type2Form;
import kvv.controllers.shared.ControllerDescr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ControllersPage extends Composite {

	private VerticalPanel vertPanel = new VerticalPanel();
	private final Set<ControlComposite> objects = new HashSet<ControlComposite>();

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

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

		controllersService
				.getControllers(new CallbackAdapter<ControllerDescr[]>() {
					@Override
					public void onSuccess(ControllerDescr[] result) {
						for (ControllerDescr descr : result) {
							if (descr != null) {
								switch (descr.type) {
								case TYPE1: {
									ControlComposite control = new Type1Form(
											descr.addr, descr.name);
									objects.add(control);
									vertPanel.add(control);
									break;
								}
								case TYPE2: {
									ControlComposite control = new Type2Form(
											descr.addr, descr.name);
									objects.add(control);
									vertPanel.add(control);
									break;
								}
								}
							}
						}
					}
				});

		initWidget(vertPanel);
	}

}
