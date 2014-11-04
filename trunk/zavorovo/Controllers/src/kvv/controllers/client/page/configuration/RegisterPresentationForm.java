package kvv.controllers.client.page.configuration;

import kvv.controllers.client.page.config.TextWithLabel;
import kvv.controllers.shared.RegisterPresentation;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class RegisterPresentationForm extends Composite {
	HorizontalPanel hp = new HorizontalPanel();
	TextWithLabel name = new TextWithLabel("", 150, false);
	TextWithLabel heigth = new TextWithLabel("Высота", 30, true);
	TextWithLabel min = new TextWithLabel("Min", 30, true);
	TextWithLabel max = new TextWithLabel("Max", 30, true);
	TextWithLabel step = new TextWithLabel("Шаг", 30, true);

	String tostr(Integer i) {
		return i == null ? "" : "" + i;
	}

	public RegisterPresentationForm(RegisterPresentation rp) {

		String name = rp == null ? "" : rp.name;
		String h = rp == null ? "" : tostr(rp.height);
		String min = rp == null ? "" : tostr(rp.min);
		String max = rp == null ? "" : tostr(rp.max);
		String step = rp == null ? "" : tostr(rp.step);

		hp.setSpacing(2);

		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		this.name.textBox.setText(name);
		hp.add(this.name);

		this.heigth.textBox.setText(h);
		hp.add(this.heigth);

		this.min.textBox.setText(min);
		hp.add(this.min);

		this.max.textBox.setText(max);
		hp.add(this.max);

		this.step.textBox.setText(step);
		hp.add(this.step);

		initWidget(hp);
	}

	public RegisterPresentation get(ErrorLocation errLoc) {
		RegisterPresentation rp = new RegisterPresentation();
		errLoc.rp = rp;
		rp.name = name.getText();
		rp.height = heigth.getNum();
		rp.min = min.getNum();
		rp.max = max.getNum();
		rp.step = step.getNum();
		return rp;
	}
}
