package kvv.controllers.client.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.SourcesService;
import kvv.controllers.client.SourcesServiceAsync;
import kvv.controllers.client.controls.ControlComposite;
import kvv.controllers.client.controls.simple.GetRegControl;
import kvv.controllers.client.controls.simple.GetSetRegControl;
import kvv.controllers.client.controls.simple.SimpleRelayControl;
import kvv.controllers.register.RegisterDescr;
import kvv.controllers.register.SourceDescr;
import kvv.controllers.shared.ControllerDescr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

	private final MultiMap<Integer, ControlComposite> objects = new MultiMap<Integer, ControlComposite>();

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
													objects.put(
															controllerDescr.addr,
															form);
												}

												if (--cnt[0] == 0)
													refresh();
											}
										});
							}
						}

					}
				});
		/*
		 * controllersService.getObjects(new CallbackAdapter<ObjectDescr[]>() {
		 * 
		 * @Override public void onSuccess(ObjectDescr[] result) {
		 * HorizontalPanel horizPanel = new HorizontalPanel();
		 * horizPanel.setBorderWidth(1); // horizPanel.setSpacing(5); for
		 * (ObjectDescr descr : result) { if (descr != null) { switch
		 * (descr.type) { case RELAY: RelayControl relayControl = new
		 * RelayControl( descr.addr, descr.register, descr.name);
		 * horizPanel.add(relayControl);
		 * 
		 * objects.put(descr.addr, relayControl); break; case HOTHOUSE:
		 * HothouseControl hothouseControl = new HothouseControl( descr.addr,
		 * descr.name); horizPanel.add(hothouseControl); objects.put(descr.addr,
		 * hothouseControl); break; case SEPARATOR: vertPanel.add(horizPanel);
		 * horizPanel = new HorizontalPanel(); horizPanel.setBorderWidth(1); //
		 * horizPanel.setSpacing(5); break; } } } vertPanel.add(horizPanel);
		 * refresh();
		 * 
		 * } });
		 */
		initWidget(vertPanel);
	}

	public void refresh() {
		for (final Integer addr : objects.keySet()) {
			Set<ControlComposite> set = objects.get(addr);
			for (ControlComposite c : set) {
				c.refresh(null);
			}

			controllersService.getRegs(addr,
					new AsyncCallback<Map<Integer, Integer>>() {
						@Override
						public void onFailure(Throwable caught) {
						}

						@Override
						public void onSuccess(Map<Integer, Integer> result) {
							Set<ControlComposite> set = objects.get(addr);
							for (ControlComposite c : set) {
								c.refresh(result);
							}
						}
					});
		}

	}

	static class Form extends ControlComposite {
		Collection<ControlComposite> objects = new ArrayList<ControlComposite>();

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
					objects.add(cb);
					break;
				case edit:
					ControlComposite control;
					if (reg.editable)
						control = new GetSetRegControl(addr, reg.reg, false, "");
					else
						control = new GetRegControl(addr, reg.reg, false, "");
					grid.setWidget(row, 0, new Label(reg.text));
					grid.setWidget(row, 1, control);
					objects.add(control);
					break;
				default:
					break;
				}
				row++;
			}

			initWidget(panel);
		}

		@Override
		public void refresh(Map<Integer, Integer> result) {
			for (ControlComposite composite : objects)
				composite.refresh(result);
		}
	}
}
