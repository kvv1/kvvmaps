package kvv.goniometer.ui.props;

@SuppressWarnings("serial")
public class FloatPropertyPanel extends PropertyPanel {

	public FloatPropertyPanel(String propName, String label) {
		super(propName, label);
	}

	public FloatPropertyPanel(String propName, String label, String defaultValue) {
		super(propName, label, defaultValue);
	}

	@Override
	boolean check(String val) {
		try {
			Float.parseFloat(val);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
