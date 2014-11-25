package kvv.controllers.client.page.configuration;

import java.util.List;

import kvv.controllers.client.Controllers;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class ControllerForm extends Composite {
	HorizontalPanel hp = new HorizontalPanel();
	CheckBox enabled = new CheckBox("Вкл.");
	TextWithLabel name = new TextWithLabel("", 150, false);
	TextWithLabel addr = new TextWithLabel("Адрес", 30, true);
	ListBox type = new ListBox();
	{
		type.addItem("");
		for (String t : Controllers.systemDescr.controllerTypes.keySet())
			type.addItem(t);
	}

	public ControllerForm(ControllerDescr cd) {
		String name = cd == null ? "" : cd.name;
		int addr = cd == null ? 0 : cd.addr;

		if (cd != null)
			for (int i = 0; i < this.type.getItemCount(); i++)
				if (this.type.getItemText(i).equals(cd.type))
					this.type.setSelectedIndex(i);

		boolean en = cd == null ? true : cd.enabled;

		hp.setSpacing(2);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		enabled.setValue(en);
		this.name.textBox.setText(name);
		this.addr.textBox.setText("" + addr);
		hp.add(enabled);
		hp.add(this.name);
		hp.add(this.addr);
		hp.add(this.type);
		initWidget(hp);
	}

	public ControllerDescr get(ErrorLocation errLoc, List<RegisterForm> regForms) {
		errLoc.clear();
		ControllerDescr cd = new ControllerDescr();
		errLoc.cd = cd;
		cd.name = name.getText();
		cd.addr = addr.getNum();
		cd.type = type.getItemText(type.getSelectedIndex());
		cd.enabled = enabled.getValue();

		cd.registers = new RegisterDescr[regForms.size()];

		for (int i = 0; i < regForms.size(); i++)
			cd.registers[i] = regForms.get(i).get(errLoc);

		return cd;

	}
}