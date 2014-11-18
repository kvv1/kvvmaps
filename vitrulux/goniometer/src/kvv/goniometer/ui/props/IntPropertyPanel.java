package kvv.goniometer.ui.props;

@SuppressWarnings("serial")
public class IntPropertyPanel extends PropertyPanel {

	public IntPropertyPanel(String propName, String label) {
		super(propName, label);
	}

	public IntPropertyPanel(String propName, String label, String defaultValue) {
		super(propName, label, defaultValue);
	}

	@Override
	boolean check(String val) {
		try {
			Integer.parseInt(val);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
