package kvv.controllers.client.page.configuration;

import java.util.ArrayList;
import java.util.List;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.gwtutils.client.DetPanel;
import kvv.gwtutils.client.form.EditablePanel;

public class ControllersTree extends
		EditablePanel<DetPanel<ControllerForm, EditablePanel<RegisterForm>>> {

	public ControllersTree() {
		super(false, "Новый контроллер");
	}

	@Override
	protected DetPanel<ControllerForm, EditablePanel<RegisterForm>> createItem() {

		RegsForm regsForm = new RegsForm();
		ControllerForm controllerForm = new ControllerForm(null, regsForm);
		return new DetPanel<ControllerForm, EditablePanel<RegisterForm>>(
				controllerForm, regsForm);
	}

	public void set(ControllerDescr[] controllers) {
		for (final ControllerDescr controllerDescr : controllers) {
			RegsForm regsForm = new RegsForm();
			regsForm.setControllerType(controllerDescr.type);

			for (RegisterDescr reg : controllerDescr.registers) {
				regsForm.add(new RegisterForm(reg));
			}

			ControllerForm controllerForm = new ControllerForm(controllerDescr,
					regsForm);

			add(new DetPanel<ControllerForm, EditablePanel<RegisterForm>>(
					controllerForm, regsForm));
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

	static class RegsForm extends EditablePanel<RegisterForm> {

		public RegsForm() {
			super(false, "Новый регистр");
		}

		private String type;

		public void setControllerType(String type) {
			this.type = type;
			for (RegisterForm rf : getItems())
				rf.setControllerType(type);
		}

		@Override
		protected RegisterForm createItem() {
			RegisterForm rf = new RegisterForm(null);
			rf.setControllerType(type);
			return rf;
		}

		@Override
		public void add(RegisterForm widget) {
			super.add(widget);
			widget.setControllerType(type);
		}

	}

}
