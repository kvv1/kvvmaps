package kvv.controllers.client.control.simple;

import kvv.controllers.client.control.AllRegs;
import kvv.controllers.client.control.ChildComposite;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class GetRegControlTime extends ChildComposite {

	private final HorizontalPanel panel = new HorizontalPanel();

	private final Label label;
	private final Label edit;
	private final int reg;

	public GetRegControlTime(final int addr, final int reg, final String text) {
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
		edit.setWidth("60px");
		edit.setText("???");
		panel.add(edit);

		initWidget(panel);
	}

	@Override
	public void refresh(AllRegs regs) {
		edit.setText("???");

		if (regs == null)
			return;

		Integer _valHi = regs.values.get(reg);
		Integer _valLo = regs.values.get(reg + 1);

		if (_valHi == null || _valLo == null)
			return;

		int t = (_valHi << 16) + (_valLo & 0xFFFF);

		String s = convertSecondsToHMmSs(t / 1000);

		edit.setText(s);
	}

	public static String convertSecondsToHMmSs(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;

		return NumberFormat.getFormat("00").format(h) + ":"
				+ NumberFormat.getFormat("00").format(m) + ":"
				+ NumberFormat.getFormat("00").format(s);
	}
}
