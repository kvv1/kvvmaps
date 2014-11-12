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
import kvv.goniometer.ui.utils.FlowWrapper;

@SuppressWarnings("serial")
public abstract class MotorPanel extends JPanel {

	JButton startAbsButton = new JButton("Старт абс.");
	JButton startDegButton = new JButton("Старт град.");
	JTextField posTo = new JTextField(10);

	JButton stopButton = new JButton("Стоп");
	JButton zeroButton = new JButton("Ноль");
	JButton zeroOkButton = new JButton("Ноль Ok");
	JLabel posAbs = new JLabel("xaxa");
	JLabel posDeg = new JLabel("xaxa");
	JLabel status = new JLabel(" ");

	protected abstract int getRange();

	protected abstract float getDegStart();

	protected abstract float getDegEnd();

	private void update(Motor motor) {
		startAbsButton.setEnabled(motor.completed() && motor.getPos() >= 0);
		startDegButton.setEnabled(motor.completed() && motor.getPos() >= 0);
		int p = motor.getPos();
		if (p < 0) {
			posAbs.setText("Позиция: Неизвестно");
			posDeg.setText("Угол: Неизвестно");
		} else {
			float deg = getDegStart() + p * (getDegEnd() - getDegStart())
					/ getRange();
			posAbs.setText("Позиция: " + p + " (" + getRange() + ")");
			posDeg.setText("Угол: " + deg + " (" + getDegStart() + "..."
					+ getDegEnd() + ")");
		}
	}

	public MotorPanel(final Motor motor, String name) {
		// setLayout(new FlowLayout());

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
					if (p < 0 || p > getRange())
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
					if (d < getDegStart() || d > getDegEnd())
						status.setText("Заданная позиция вне диапазона");
					else {
						status.setText(" ");
						motor.moveTo((int) ((d - getDegStart()) * getRange() / (getDegEnd() - getDegStart())));
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
