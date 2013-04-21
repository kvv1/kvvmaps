package kvv.controllers.client.pages;

import java.util.Map;
import java.util.Set;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.controls.ControlComposite;
import kvv.controllers.client.controls.form.HothouseForm;
import kvv.controllers.client.controls.form.RelayForm;
import kvv.controllers.shared.ObjectDescr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ObjectsPage extends Composite {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private final VerticalPanel vertPanel = new VerticalPanel();

	private final MultiMap<Integer, ControlComposite> objects = new MultiMap<Integer, ControlComposite>();

	public ObjectsPage() {

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

		controllersService.getObjects(new CallbackAdapter<ObjectDescr[]>() {
			@Override
			public void onSuccess(ObjectDescr[] result) {
				HorizontalPanel horizPanel = new HorizontalPanel();
				horizPanel.setBorderWidth(1);
				// horizPanel.setSpacing(5);
				for (ObjectDescr descr : result) {
					if (descr != null) {
						switch (descr.type) {
						case RELAY:
							RelayForm relayControl = new RelayForm(
									descr.addr, descr.register, descr.name);
							horizPanel.add(relayControl);

							objects.put(descr.addr, relayControl);
							break;
						case HOTHOUSE:
							HothouseForm hothouseControl = new HothouseForm(
									descr.addr, descr.name);
							horizPanel.add(hothouseControl);
							objects.put(descr.addr, hothouseControl);
							break;
						case SEPARATOR:
							vertPanel.add(horizPanel);
							horizPanel = new HorizontalPanel();
							horizPanel.setBorderWidth(1);
							// horizPanel.setSpacing(5);
							break;
						}
					}
				}
				vertPanel.add(horizPanel);
				refresh();

			}
		});

		initWidget(vertPanel);
	}

	public void refresh() {
		for (final Integer addr : objects.keySet()) {
			Set<ControlComposite> set = objects.get(addr);
			for (ControlComposite c : set) {
				c.refresh(null);
				// System.out.println("refr " + addr);
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
}
