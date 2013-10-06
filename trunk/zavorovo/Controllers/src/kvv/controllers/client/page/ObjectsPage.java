package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.List;

import kvv.controllers.shared.ControllerDescr;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ObjectsPage extends Composite {
	private List<Form> objects = new ArrayList<Form>();

	public ObjectsPage() {
		final VerticalPanel vertPanel = new VerticalPanel();
		vertPanel.setSpacing(10);
		// flowPanel.setBorderWidth(1);

		Button refreshButton = new Button("Обновить");
		vertPanel.add(refreshButton);
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		HorizontalPanel horizPanel = new HorizontalPanel();
		horizPanel.setBorderWidth(1);
		// horizPanel.setSpacing(5);
		for (String name : ControllersPage.forms) {
			if (name == null) {
				vertPanel.add(horizPanel);
				horizPanel = new HorizontalPanel();
				horizPanel.setBorderWidth(1);
			} else {
				int addr = -1;
				for (ControllerDescr descr : ControllersPage.controllers)
					if (descr.name.equals(name))
						addr = descr.addr;

				if (addr >= 0) {
					Form form = new Form(addr, name);
					horizPanel.add(form);
					objects.add(form);
				} else {
					Window.alert("Неизвестное имя контроллера '" + name + "'");
				}
			}
		}
		vertPanel.add(horizPanel);
		refresh();

		initWidget(vertPanel);
	}

	public void refresh() {
		for (final Form form : objects)
			form.refreshUI();
	}
}
