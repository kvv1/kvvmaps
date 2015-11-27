package kvv.controllers.client.page.configuration;

import java.util.HashMap;

import kvv.controllers.client.Controllers;
import kvv.controllers.shared.ControllerDef.RegisterDef;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.RegisterDescr;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class RegisterForm extends Composite {
	private final HorizontalPanel hp = new HorizontalPanel();
	private final TextWithLabel name = new TextWithLabel("", 150, false);
	private final ListBox reg = new ListBox();

	private final HashMap<Integer, Integer> regsMap = new HashMap<Integer, Integer>();

	public RegisterForm(RegisterDescr rd) {
		if (rd == null)
			rd = new RegisterDescr();

		hp.setSpacing(2);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		this.name.textBox.setText(rd.name);

		for (int i = 0; i < 200; i++) {
			regsMap.put(i, i);
			reg.addItem("" + i);
			if (i == rd.register)
				reg.setSelectedIndex(reg.getItemCount() - 1);
		}

		hp.add(this.name);
		hp.add(this.reg);
		initWidget(hp);
	}

	public RegisterDescr get(ErrorLocation errLoc) {
		RegisterDescr rd = new RegisterDescr();
		errLoc.rd = rd;
		rd.name = name.getText();
		rd.register = regsMap.get(reg.getSelectedIndex());
		return rd;
	}

	public void setControllerType(String type) {
		Integer register = null;

		int idx = reg.getSelectedIndex();
		if (idx >= 0)
			register = regsMap.get(idx);
		if (register == null)
			register = 0;

		reg.clear();
		regsMap.clear();

		ControllerType controllerType = Controllers.systemDescr.controllerTypes
				.get(type);

		if (controllerType != null) {
			boolean found = false;
			for (RegisterDef rdef : controllerType.def.registers) {
				regsMap.put(reg.getItemCount(), rdef.n);
				reg.addItem(rdef.name);
				if (rdef.n == register) {
					reg.setSelectedIndex(reg.getItemCount() - 1);
					found = true;
				}
			}
			if (!found) {
				regsMap.put(reg.getItemCount(), register);
				reg.addItem("" + register);
				reg.setSelectedIndex(reg.getItemCount() - 1);
			}
		} else {
			for (int i = 0; i < 200; i++) {
				regsMap.put(i, i);
				reg.addItem("" + i);
				if (i == register)
					reg.setSelectedIndex(reg.getItemCount() - 1);
			}
		}

	}
}