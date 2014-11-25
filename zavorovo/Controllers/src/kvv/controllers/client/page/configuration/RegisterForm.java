package kvv.controllers.client.page.configuration;

import kvv.controllers.shared.RegisterDescr;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class RegisterForm extends Composite {
	HorizontalPanel hp = new HorizontalPanel();
	TextWithLabel name = new TextWithLabel("", 150, false);
	TextWithLabel num = new TextWithLabel("N", 30, true);

	public RegisterForm(RegisterDescr rd) {
		String name = rd == null ? "" : rd.name;
		int num = rd == null ? 0 : rd.register;
		hp.setSpacing(2);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		this.name.textBox.setText(name);
		this.num.textBox.setText("" + num);
		hp.add(this.name);
		hp.add(this.num);
		initWidget(hp);
	}

	public RegisterDescr get(ErrorLocation errLoc) {
		RegisterDescr rd = new RegisterDescr();
		errLoc.rd = rd;
		rd.name = name.getText();
		rd.register = num.getNum();
		return rd;
	}
}