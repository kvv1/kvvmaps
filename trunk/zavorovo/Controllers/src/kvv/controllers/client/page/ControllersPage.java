package kvv.controllers.client.page;

import java.util.HashSet;
import java.util.Set;

import kvv.controllers.client.Controllers;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.form.MU110_8Form;
import kvv.controllers.client.control.form.Type2Form;
import kvv.controllers.shared.ControllerDescr;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ControllersPage extends Composite {

	private VerticalPanel vertPanel = new VerticalPanel();
	private final Set<ControlComposite> objects = new HashSet<ControlComposite>();

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

		for (ControllerDescr descr : Controllers.systemDescr.controllerDescrs) {
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
