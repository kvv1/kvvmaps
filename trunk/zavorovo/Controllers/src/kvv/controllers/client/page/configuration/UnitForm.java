package kvv.controllers.client.page.configuration;

import java.util.List;

import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.UnitDescr;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class UnitForm extends Composite {
	HorizontalPanel hp = new HorizontalPanel();
	TextWithLabel name = new TextWithLabel("", 150, false);

	public UnitForm(UnitDescr unit) {
		hp.setSpacing(2);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		this.name.textBox.setText(unit == null ? "" : unit.name);
		hp.add(this.name);
		initWidget(hp);
	}

	public UnitDescr get(ErrorLocation errLoc, List<RegisterPresentationForm> regForms) {
		errLoc.clear();
		UnitDescr ud = new UnitDescr();
		errLoc.ud = ud;
		ud.name = name.getText();

		ud.registers = new RegisterPresentation[regForms.size()];

		for (int i = 0; i < regForms.size(); i++)
			ud.registers[i] = regForms.get(i).get(errLoc);

		return ud;

	}
}