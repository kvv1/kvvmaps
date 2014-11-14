package kvv.goniometer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import kvv.goniometer.ui.mainpage.ScanParams;
import kvv.goniometer.ui.motor.MotorPanel;
import kvv.goniometer.ui.props.Prop;
import kvv.goniometer.ui.props.PropertiesPanel;
import kvv.goniometer.ui.sensor.SensorPanel;
import kvv.goniometer.ui.utils.FlowWrapper;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

	public ControlPanel(final Motor smsdX, final Motor smsdY,
			ScanParams scanParamsX, ScanParams scanParamsY,
			final Sensor sensor, final PropertiesPanel propertiesPanel) {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new FlowWrapper(FlowLayout.LEFT, new MotorPanel(smsdX,
				"Азимутальный мотор", scanParamsX)));
		panel.add(new FlowWrapper(FlowLayout.LEFT, new MotorPanel(smsdY,
				"Полярный мотор", scanParamsY)));

		panel.add(new FlowWrapper(FlowLayout.LEFT, new SensorPanel(sensor)));

		add(panel, BorderLayout.PAGE_START);
	}
}
