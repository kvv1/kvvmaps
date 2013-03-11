package kvv.controllers.client.pages;

import java.util.HashSet;
import java.util.Set;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.controls.ControlComposite;
import kvv.controllers.client.controls.Type1Control;
import kvv.controllers.shared.ControllerDescr;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ControllersPage extends Composite {

	private VerticalPanel vertPanel = new VerticalPanel();
	private final Set<ControlComposite> objects = new HashSet<ControlComposite>();

	public ControllersPage(final ControllersServiceAsync controllersService) {

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
								case TYPE1:
									ControlComposite control = new Type1Control(
											descr.addr, descr.name,
											controllersService);
									objects.add(control);
									vertPanel.add(control);
									break;
								}
							}
						}
					}
				});

		initWidget(vertPanel);
	}

}
