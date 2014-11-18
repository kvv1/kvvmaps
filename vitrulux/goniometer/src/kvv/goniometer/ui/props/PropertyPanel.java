package kvv.goniometer.ui.props;

import java.awt.Dimension;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class PropertyPanel extends JPanel {

	public final String propName;
	public final String defaultValue;
	private final JTextField text = new JTextField(8);
	private final String label;

	public PropertyPanel(String propName, String label) {
		this(propName, label, "0");
	}

	public PropertyPanel(String propName, String label, String defaultValue) {
		this.label = label;
		this.propName = propName;
		this.defaultValue = defaultValue;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JLabel jlabel = new JLabel(label);
		jlabel.setPreferredSize(new Dimension(360, 0));
		add(jlabel);
		add(text);
	}

	boolean check(String val) {
		return true;
	}

	public void put(Properties properties) {
		if (!check(text.getText())) {
			JOptionPane.showMessageDialog(
					null,
					"Недопустимое значение свойства '" + label + "' : "
							+ text.getText());
			text.setText(defaultValue);
		}
		properties.put(propName, text.getText());
	}

	public void get(Properties properties) {
		text.setText(properties.getProperty(propName, defaultValue));
		if (defaultValue.length() > 0)
			put(properties);
	}

}
