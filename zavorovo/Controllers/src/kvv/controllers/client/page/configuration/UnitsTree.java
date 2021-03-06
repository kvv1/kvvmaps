package kvv.controllers.client.page.configuration;

import java.util.ArrayList;
import java.util.List;

import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.UnitDescr;
import kvv.gwtutils.client.DetPanel;
import kvv.gwtutils.client.form.EditablePanel;

public class UnitsTree
		extends
		EditablePanel<DetPanel<UnitForm, EditablePanel<RegisterPresentationForm>>> {

	public UnitsTree() {
		super(false, "Новая страница");
	}

	@Override
	protected DetPanel<UnitForm, EditablePanel<RegisterPresentationForm>> createItem() {
		DetPanel<UnitForm, EditablePanel<RegisterPresentationForm>> item = new DetPanel<UnitForm, EditablePanel<RegisterPresentationForm>>(
				new UnitForm(null),
				new EditablePanel<RegisterPresentationForm>(false,
						"Новый регистр") {
					@Override
					protected RegisterPresentationForm createItem() {
						return new RegisterPresentationForm(null);
					}
				});
		return item;
	}

	public void set(UnitDescr[] controllers) {
		for (UnitDescr controllerDescr : controllers) {
			EditablePanel<RegisterPresentationForm> registers = new EditablePanel<RegisterPresentationForm>(
					false, "Новый регистр") {
				@Override
				protected RegisterPresentationForm createItem() {
					return new RegisterPresentationForm(null);
				}
			};

			for (RegisterPresentation reg : controllerDescr.registers) {
				registers.add(new RegisterPresentationForm(reg));

			}

			add(new DetPanel<UnitForm, EditablePanel<RegisterPresentationForm>>(
					new UnitForm(controllerDescr), registers));
		}

	}

	public List<UnitDescr> get(ErrorLocation errLoc) {
		List<UnitDescr> res = new ArrayList<UnitDescr>();
		for (DetPanel<UnitForm, EditablePanel<RegisterPresentationForm>> p : getItems()) {
			UnitDescr cd = p.label.get(errLoc, p.details.getItems());
			res.add(cd);
		}
		return res;
	}

}
