package kvv.controllers.client.page.configuration;

import java.util.ArrayList;
import java.util.List;

import kvv.controllers.client.control.form.DetPanel;
import kvv.controllers.client.control.form.EditablePanel;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;

public class ControllersTree extends
		EditablePanel<DetPanel<ControllerForm, EditablePanel<RegisterForm>>> {

	public ControllersTree() {
		super(!ModePage.controlMode, "Новый контроллер");
	}

	@Override
	protected DetPanel<ControllerForm, EditablePanel<RegisterForm>> createItem() {
		DetPanel<ControllerForm, EditablePanel<RegisterForm>> item = new DetPanel<ControllerForm, EditablePanel<RegisterForm>>(
				new ControllerForm(null), new EditablePanel<RegisterForm>(
						!ModePage.controlMode, "Новый регистр") {
					@Override
					protected RegisterForm createItem() {
						return new RegisterForm(null);
					}
				});
		return item;
	}

	public void set(ControllerDescr[] controllers) {
		for (ControllerDescr controllerDescr : controllers) {
			EditablePanel<RegisterForm> registers = new EditablePanel<RegisterForm>(
					!ModePage.controlMode, "Новый регистр") {
				@Override
				protected RegisterForm createItem() {
					return new RegisterForm(null);
				}
			};

			for (RegisterDescr reg : controllerDescr.registers) {
				registers.add(new RegisterForm(reg));

			}

			add(new DetPanel<ControllerForm, EditablePanel<RegisterForm>>(
					new ControllerForm(controllerDescr), registers));
		}

	}

	public List<ControllerDescr> get(ErrorLocation errLoc) {
		List<ControllerDescr> res = new ArrayList<ControllerDescr>();
		for (DetPanel<ControllerForm, EditablePanel<RegisterForm>> p : getItems()) {
			ControllerDescr cd = p.label.get(errLoc, p.details.getItems());
			res.add(cd);
		}
		return res;
	}

}
