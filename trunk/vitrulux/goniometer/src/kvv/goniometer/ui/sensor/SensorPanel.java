package kvv.goniometer.ui.sensor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import kvv.goniometer.Sensor;
import kvv.goniometer.Sensor.SensorListener;
import kvv.goniometer.SensorData;

@SuppressWarnings("serial")
public class SensorPanel extends JPanel {

	JLabel label = new JLabel();

	public SensorPanel(final Sensor sensor) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createTitledBorder("Сенсор"));
		// setPreferredSize(new Dimension(200, 200));

		add(label);

		sensor.addListener(new SensorListener() {
			@Override
			public void onChanged(final SensorData data) {
				setValue((float) data.e / 10);
			}
		});

		Timer timer = new Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (sensor.getError() != null) {
					label.setForeground(Color.red);
					label.setText(sensor.getError());
				} else {
					// setValue(sensor.getValue());
				}
			}
		});

		timer.start();

		setValue(0);
	}

	public void setValue(float e) {
		label.setForeground(Color.BLACK);
		label.setText("" + e + "                        ");
	}

}
