package kvv.goniometer.ui.motor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import kvv.goniometer.Motor;
import kvv.goniometer.Motor.MotorListener;
import kvv.goniometer.ui.mainpage.ScanParams;
import kvv.goniometer.ui.utils.FlowWrapper;

@SuppressWarnings("serial")
public class MotorPanel extends JPanel {

	private final JButton startAbsButton = new JButton("Старт абс.");
	private final JButton startDegButton = new JButton("Старт град.");
	private final JTextField posTo = new JTextField(10);

	private final JButton stopButton = new JButton("Стоп");
	private final JButton zeroButton = new JButton("Ноль");
	private final JButton zeroOkButton = new JButton("Ноль Ok");
	private final JLabel posAbs = new JLabel("xaxa");
	private final JLabel posDeg = new JLabel("xaxa");
	private final JLabel status = new JLabel(" ");

	private final ScanParams scanParams;

	private void update(Motor motor) {
		startAbsButton.setEnabled(motor.completed() && motor.getPos() >= 0);
		startDegButton.setEnabled(motor.completed() && motor.getPos() >= 0);
		int p = motor.getPos();
		if (p < 0) {
			posAbs.setText("Позиция: Неизвестно");
			posDeg.setText("Угол: Неизвестно");
		} else {
			float deg = scanParams.getDegStart() + p
					* (scanParams.getDegEnd() - scanParams.getDegStart())
					/ scanParams.getRange();
			posAbs.setText("Позиция: " + p + " (" + scanParams.getRange() + ")");
			posDeg.setText("Угол: " + deg + " (" + scanParams.getDegStart()
					+ "..." + scanParams.getDegEnd() + ")");
		}
	}

	public MotorPanel(final Motor motor, String name, ScanParams scanParams) {
		this.scanParams = scanParams;

		setBorder(BorderFactory.createTitledBorder(name));
		status.setForeground(new Color(255, 0, 0));

		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new FlowWrapper(FlowLayout.LEFT, posTo, startAbsButton,
				startDegButton));
		panel.add(new FlowWrapper(FlowLayout.LEFT, posAbs));
		panel.add(new FlowWrapper(FlowLayout.LEFT, posDeg));
		panel.add(new FlowWrapper(FlowLayout.LEFT, status));

		add(panel, BorderLayout.PAGE_START);
		add(new FlowWrapper(FlowLayout.LEFT, zeroButton, zeroOkButton,
				stopButton), BorderLayout.PAGE_END);

		motor.addListener(new MotorListener() {
			@Override
			public void onChanged() {
				update(motor);
			}
		});

		setButtons(motor);

		update(motor);
	}

	private void setButtons(final Motor motor) {
		startAbsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int p = Integer.parseInt(posTo.getText());
					if (p < 0 || p > scanParams.getRange())
						status.setText("Заданная позиция вне диапазона");
					else {
						status.setText(" ");
						motor.moveTo(p);
					}
				} catch (Exception e1) {
					status.setText(e1.getClass().getSimpleName() + " "
							+ e1.getMessage());
				}
			}
		});

		startDegButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					float d = Float.parseFloat(posTo.getText());
					if (d < scanParams.getDegStart()
							|| d > scanParams.getDegEnd())
						status.setText("Заданная позиция вне диапазона");
					else {
						status.setText(" ");
						motor.moveTo((int) ((d - scanParams.getDegStart())
								* scanParams.getRange() / (scanParams
								.getDegEnd() - scanParams.getDegStart())));
					}
				} catch (Exception e1) {
					status.setText(e1.getClass().getSimpleName() + " "
							+ e1.getMessage());
				}
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					status.setText(" ");
					motor.stop();
				} catch (Exception e1) {
					status.setText(e1.getClass().getSimpleName() + " "
							+ e1.getMessage());
				}
			}
		});

		zeroButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					status.setText(" ");
					motor.zero();
				} catch (Exception e1) {
					status.setText(e1.getClass().getSimpleName() + " "
							+ e1.getMessage());
				}
			}
		});

		zeroOkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					status.setText(" ");
					motor.zeroOK();
				} catch (Exception e1) {
					status.setText(e1.getClass().getSimpleName() + " "
							+ e1.getMessage());
				}
			}
		});
	}

}
