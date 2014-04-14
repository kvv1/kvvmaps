package kvv.controllers.client.control.form;

import kvv.controllers.client.Controllers;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.simple.GetRegControl;
import kvv.controllers.client.control.simple.GetSetRegControl;
import kvv.controllers.client.control.simple.GetSetRegControl2;
import kvv.controllers.client.control.simple.SimpleRelayControl;
import kvv.controllers.client.control.vm.RuleControl;
import kvv.controllers.client.control.vm.VMControl;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.ControllerUI;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CommonForm extends ControlComposite {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private final int addr;
	private final String name;
	private final ControllerType controllerType;

	public CommonForm(String type, int addr, String name) {
		this.addr = addr;
		this.name = name;
		controllerType = Controllers.systemDescr.controllerTypes.get(type);

		if (controllerType != null)
			initWidget(createWidget(controllerType.ui));
		else
			initWidget(new Label("отсутствует описание типа контроллера "
					+ type));
		refresh();
	}

	private Widget createWidget(ControllerUI ui) {
		switch (ui.type) {
		case CHECKBOX:
			SimpleRelayControl cb = new SimpleRelayControl(addr, ui.reg,
					ui.label);
			add(cb);
			return cb;
		case HP:
			HorizontalPanel hp = new HorizontalPanel();
			hp.setSpacing(2);
			if (ui.align != null) {
				switch (ui.align) {
				case CENTER:
					hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
					break;
				case BEGIN:
					hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
					break;
				case END:
					hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
					break;
				}
			}
			if (ui.children != null)
				for (ControllerUI child : ui.children)
					hp.add(createWidget(child));
			return hp;
		case VP:
			VerticalPanel vp = new VerticalPanel();
			vp.setSpacing(2);
			if (ui.align != null) {
				switch (ui.align) {
				case CENTER:
					vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
					break;
				case BEGIN:
					vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
					break;
				case END:
					vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					break;
				}
			}
			if (ui.children != null)
				for (ControllerUI child : ui.children)
					vp.add(createWidget(child));
			return vp;
		case TEXT_RO:
			GetRegControl grc = new GetRegControl(addr, ui.reg, 1, ui.label);
			add(grc);
			return grc;
		case TEXT_RW:
			GetSetRegControl gsrc = new GetSetRegControl(addr, ui.reg, false,
					ui.label);
			add(gsrc);
			return gsrc;
		case TEXT2_RW:
			GetSetRegControl2 gsrc2 = new GetSetRegControl2(addr, ui.reg,
					ui.label);
			add(gsrc2);
			return gsrc2;
		case HELLO:
			Button helloButton = new Button("Hello");

			helloButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					controllersService.hello(addr,
							new AsyncCallback<Integer>() {

								@Override
								public void onSuccess(Integer result) {
									Window.alert("OK " + result);
								}

								@Override
								public void onFailure(Throwable caught) {
									Window.alert("FAILED "
											+ caught.getMessage());
								}
							});
				}
			});

			return helloButton;
		case VM:
			VMControl vmControl = new VMControl(addr, name, controllerType.def);
			add(vmControl);
			return vmControl;
		case RULES:
			RuleControl rc = new RuleControl(addr, controllerType.def);
			add(rc);
			return rc;
		case UPLOAD:
			return new UploadForm("Загрузить", GWT.getModuleBaseURL()
					+ "upload?addr=" + addr);
		default:
			return new Label("<unknown>");
		}

	}

}
