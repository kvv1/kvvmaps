package kvv.controllers.client.page.configuration;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class TextWithLabel extends Composite {
	HorizontalPanel hp = new HorizontalPanel();
	Label label;
	public TextBox textBox;

	public TextWithLabel(String lab, int w, boolean number) {
		label = new Label(lab);
		textBox = new TextBox();
		if (number) {
			textBox.addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					// TODO Auto-generated method stub

				}
			});
		} else {
			textBox = new TextBox();
		}

		textBox.setWidth("" + w + "px");
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp.add(label);
		hp.add(textBox);
		initWidget(hp);
	}

	public String getText() {
		return textBox.getText();
	}

	public Integer getNum() {
		if (textBox.getText().isEmpty())
			return null;
		return Integer.parseInt(textBox.getText());
	}

}