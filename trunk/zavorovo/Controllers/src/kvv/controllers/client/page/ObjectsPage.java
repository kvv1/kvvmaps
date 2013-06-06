package kvv.controllers.client.page;

import java.util.Set;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.register.AllRegs;
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
					if (descr == null) {
						vertPanel.add(horizPanel);
						horizPanel = new HorizontalPanel();
						horizPanel.setBorderWidth(1);
					} else {
						switch (descr.type) {
						case RELAY:
//							RelayForm relayControl = new RelayForm(descr.addr,
//									descr.reg, descr.name);
//							horizPanel.add(relayControl);
//							objects.put(descr.addr, relayControl);
							break;
						case FORM:
							Form form = new Form(descr.addr, descr.name);
							horizPanel.add(form);
							objects.put(descr.addr, form);
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
			final Set<ControlComposite> set = objects.get(addr);
			for (ControlComposite c : set) {
				if (c instanceof Form)
					((Form) c).refreshUI(null);
				else
					c.refresh(null);
			}

			controllersService.getRegs(addr, new AsyncCallback<AllRegs>() {
				@Override
				public void onSuccess(AllRegs result) {
					for (ControlComposite c : set) {
						if (c instanceof Form)
							((Form) c).refreshUI(result);
						else
							c.refresh(result);
					}
				}

				@Override
				public void onFailure(Throwable caught) {
				}
			});
		}
	}
}
