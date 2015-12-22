package kvv.controllers.client.control.form;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.gwtutils.client.CallbackAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class ResetButton extends Button {

	public ResetButton(final int addr) {
		super("Reset", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ControllersServiceAsync controllersService = GWT
						.create(ControllersService.class);

				controllersService.reset(addr, new CallbackAdapter<Void>() {
				});
			}
		});
	}
}
