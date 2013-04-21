package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Collection;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.SourcesService;
import kvv.controllers.client.SourcesServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.simple.GetRegControl;
import kvv.controllers.client.control.simple.GetSetRegControl;
import kvv.controllers.client.control.simple.SimpleRelayControl;
import kvv.controllers.register.RegisterDescr;
import kvv.controllers.register.SourceDescr;
import kvv.controllers.shared.ControllerDescr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FormsPage extends Composite {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private final SourcesServiceAsync sourcesService = GWT
			.create(SourcesService.class);

	private final VerticalPanel vertPanel = new VerticalPanel();
	private final Collection<ControlComposite> objects = new ArrayList<ControlComposite>();

	public FormsPage() {

		vertPanel.setSpacing(10);
		vertPanel.setBorderWidth(1);
		// flowPanel.setBorderWidth(1);

		Button refreshButton = new Button("Обновить");
		vertPanel.add(refreshButton);
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		final int[] cnt = new int[1];

		controllersService
				.getControllers(new CallbackAdapter<ControllerDescr[]>() {
					@Override
					public void onSuccess(ControllerDescr[] result) {
						for (final ControllerDescr controllerDescr : result) {
							if (controllerDescr != null) {
								cnt[0]++;
								sourcesService.getSourceDescr(
										controllerDescr.name,
										new CallbackAdapter<SourceDescr>() {
											@Override
											public void onSuccess(
													SourceDescr sourceDescr) {
												if (sourceDescr != null) {
													Form form = new Form(
															controllerDescr.addr,
															controllerDescr.name,
															sourceDescr);
													vertPanel.add(form);
													objects.add(form);
												}

												if (--cnt[0] == 0)
													refresh();
											}
										});
							}
						}

					}
				});
		initWidget(vertPanel);
	}

	public void refresh() {
		for (ControlComposite controlComposite : objects) {
			controlComposite.refresh();
		}
	}

	static class Form extends ControlComposite {

		public Form(int addr, String name, SourceDescr sourceDescr) {
			super(addr);

			VerticalPanel panel = new VerticalPanel();
			Grid grid = new Grid(sourceDescr.registers.length, 2);

			panel.add(new Label(name));
			panel.add(grid);

			Button refresh = new Button("Обновить");
			panel.add(refresh);

			refresh.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					refresh();
				}
			});

			int row = 0;
			for (RegisterDescr reg : sourceDescr.registers) {
				switch (reg.type) {
				case checkbox:
					SimpleRelayControl cb = new SimpleRelayControl(addr,
							reg.reg, null);
					grid.setWidget(row, 0, new Label(reg.text));
					grid.setWidget(row, 1, cb);
					add(cb);
					break;
				case edit:
					ControlComposite control;
					if (reg.editable)
						control = new GetSetRegControl(addr, reg.reg, false, "");
					else
						control = new GetRegControl(addr, reg.reg, false, "");
					grid.setWidget(row, 0, new Label(reg.text));
					grid.setWidget(row, 1, control);
					add(control);
					break;
				default:
					break;
				}
				row++;
			}

			initWidget(panel);
		}
	}
}
