package kvv.controllers.client.control.simple;

import kvv.controllers.client.control.AllRegs;
import kvv.controllers.client.control.ChildComposite;

import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class GetRegControl extends ChildComposite {

	private final HorizontalPanel panel = new HorizontalPanel();

	private final Label label;
	private final Label edit;
	private final int reg;

	public GetRegControl(final int addr, final int reg, final String text) {
		super(addr);
		this.reg = reg;

		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		if (text != null) {
			this.label = new Label(text);
			panel.add(label);
		} else {
			label = null;
		}

		this.edit = new Label();
		edit.setWidth("40px");
		edit.setText("???");
		panel.add(edit);

		initWidget(panel);
	}

	@Override
	public void refresh(AllRegs regs) {
		edit.setText("???");

		if (regs == null)
			return;

		Integer _val = regs.values.get(reg);

		if (_val == null)
			return;

		edit.setText(Integer.toString(_val));
	}
}
