package kvv.controllers.client.controls.simple;

import java.util.Map;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.controls.ControlComposite;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class GetRegControl extends ControlComposite {

	private final HorizontalPanel panel = new HorizontalPanel();

	private final Label label;
	private final Label edit;
	private final int reg;
	private final boolean div10;

	public GetRegControl(final int addr, final int reg, final boolean div10,
			String text) {
		super(addr);
		this.label = new Label(text);
		this.edit = new Label();
		this.reg = reg;
		this.div10 = div10;

		edit.setWidth("40px");
		edit.setText("???");

		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		panel.add(label);
		panel.add(edit);

		initWidget(panel);
	}

	public void refresh(Map<Integer, Integer> regs) {
		edit.setText("???");

		if (regs == null)
			return;

		Integer _val = regs.get(reg);

		if (_val == null)
			return;

		if (div10)
			edit.setText(Float.toString((float) _val / 10));
		else
			edit.setText(Integer.toString(_val));
	}
}
