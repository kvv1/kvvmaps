package kvv.controllers.client.page;

import java.util.HashSet;
import java.util.Set;

import kvv.controllers.client.Controllers;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.form.CommonForm;
import kvv.controllers.shared.ControllerDescr;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
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

		ControllerDescr[] controllerDescrs = Controllers.systemDescr.controllers;

		Grid grid = new Grid(controllerDescrs.length, 4);
		vertPanel.add(grid);

		int row = 0;

		for (ControllerDescr descr : controllerDescrs) {
			if (descr != null && descr.addr != 0) {
				grid.setWidget(row, 0, new Label(descr.name));
				grid.setWidget(row, 1, new Label("addr:" + descr.addr));

				final ControlComposite control = new CommonForm(
						descr.type, descr.addr, descr.name);
				objects.add(control);
				
				grid.setWidget(row, 2, control);

				Button refreshButton1 = new Button("Обновить");
				grid.setWidget(row, 3, refreshButton1);

				refreshButton1.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						control.refresh();
					}
				});

				row++;
			}
		}
		initWidget(vertPanel);
	}

}
