package kvv.goniometer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import kvv.goniometer.ui.motor.MotorPanel;
import kvv.goniometer.ui.props.Prop;
import kvv.goniometer.ui.props.PropertiesPanel;
import kvv.goniometer.ui.sensor.SensorPanel;
import kvv.goniometer.ui.utils.FlowWrapper;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

	public ControlPanel(final Motor smsdX, final Motor smsdY,
			final Sensor sensor, final PropertiesPanel propertiesPanel) {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new FlowWrapper(FlowLayout.LEFT, new MotorPanel(smsdX,
				"Азимутальный мотор") {

			@Override
			protected int getRange() {
				return Integer.parseInt(propertiesPanel.properties.getProperty(
						Prop.X_RANGE, "0"));
			}

			@Override
			protected float getDegStart() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.X_START_DEGREES, "0"));
			}

			@Override
			protected float getDegEnd() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.X_END_DEGREES, "0"));
			}
		}));
		panel.add(new FlowWrapper(FlowLayout.LEFT, new MotorPanel(smsdY,
				"Полярный мотор") {
			@Override
			protected int getRange() {
				return Integer.parseInt(propertiesPanel.properties.getProperty(
						Prop.Y_RANGE, "0"));
			}

			@Override
			protected float getDegStart() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.Y_START_DEGREES, "0"));
			}

			@Override
			protected float getDegEnd() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.Y_END_DEGREES, "0"));
			}
		}));

		panel.add(new FlowWrapper(FlowLayout.LEFT, new SensorPanel(sensor)));

		add(panel, BorderLayout.PAGE_START);
	}
}
