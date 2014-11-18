package kvv.goniometer.ui.props;

@SuppressWarnings("serial")
public class BoolPropertyPanel extends PropertyPanel {

	public BoolPropertyPanel(String propName, String label) {
		super(propName, label);
	}

	public BoolPropertyPanel(String propName, String label, String defaultValue) {
		super(propName, label, defaultValue);
	}

	@Override
	boolean check(String val) {
		return "true".equals(val) || "false".equals(val);
	}

}
