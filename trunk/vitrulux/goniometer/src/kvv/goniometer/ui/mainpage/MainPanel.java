package kvv.goniometer.ui.mainpage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import kvv.goniometer.Motor;
import kvv.goniometer.Motor.MotorListener;
import kvv.goniometer.Props;
import kvv.goniometer.Sensor;
import kvv.goniometer.SensorData;
import kvv.goniometer.Win;
import kvv.goniometer.ui.mainpage.DataSet.Data;
import kvv.goniometer.ui.props.Prop;
import kvv.goniometer.ui.utils.FlowWrapper;
import kvv.goniometer.ui.utils.HorizontalBoxPanel;
import kvv.goniometer.ui.utils.SwingLink;
import kvv.goniometer.ui.utils.VericalBoxPanel;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private final JButton startButton = new JButton("Старт");
	private final JButton stopButton = new JButton("Стоп");
	private final JButton zeroButton = new JButton("Ноль");
	private final JButton zeroOkButton = new JButton("Ноль Ok");
	private final JButton saveButton = new JButton("Сохранить данные");
	private final JLabel posDegX = new JLabel();
	private final JLabel posDegY = new JLabel();
	// JLabel value = new JLabel();
	private final JLabel status = new JLabel("_");

	private final DataSet dataSet = new DataSet();
	// private final DataCanvas canvas = new DataCanvas(dataSet);
	private final IMainView mainView;

	private final Motor motorX;
	private final Motor motorY;
	private final Sensor sensor;

	private final Props props;

	private final ScanParams scanParamsX;
	private final ScanParams scanParamsY;
	private final SensorPrams sensorPrams;

	private ScanThread thread;

	class ScanThread1 extends ScanThread {
		private final DIR primDir;
		private final float R;

		public ScanThread1(DIR primDir, float R, Motor motorPrim,
				Motor motorSec, ScanParams scanParamsPrim,
				ScanParams scanParamsSec, Sensor sensor, SensorPrams sensorPrams) {
			super(primDir == DIR.AZIMUTH ? motorPrim : motorSec,
					primDir == DIR.AZIMUTH ? motorSec : motorPrim,
					primDir == DIR.AZIMUTH ? scanParamsPrim : scanParamsSec,
					primDir == DIR.AZIMUTH ? scanParamsSec : scanParamsPrim,
					sensor, sensorPrams);
			this.primDir = primDir;
			this.R = R;
		}

		@Override
		protected void onData(float prim, float sec, SensorData data) {
			dataSet.addMeasure(primDir == DIR.AZIMUTH ? prim : sec,
					primDir == DIR.AZIMUTH ? sec : prim, R, data);
		}

		@Override
		protected void onErr(Exception e) {
			status.setText(e.getClass().getSimpleName() + " " + e.getMessage());
		}

		@Override
		protected void onFinished() {
			thread = null;
			update();
		}
	}

	public MainPanel(final Motor motorX, final Motor motorY,
			final Sensor sensor, final ScanParams scanParamsX,
			final ScanParams scanParamsY, final SensorPrams sensorPrams,
			final Props props) {
		this.motorX = motorX;
		this.motorY = motorY;
		this.sensor = sensor;
		this.props = props;
		this.scanParamsX = scanParamsX;
		this.scanParamsY = scanParamsY;
		this.sensorPrams = sensorPrams;

		mainView = new MainView(dataSet, props);
		dataSet.setWnd(mainView);

		// setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new FlowWrapper(FlowLayout.LEFT, 0, 0, mainView
				.getComponent()));
		posDegX.setAlignmentX(LEFT_ALIGNMENT);
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.PAGE_AXIS));
		statusPanel.add(posDegX);
		statusPanel.add(posDegY);
		statusPanel.add(status);

		status.setForeground(Color.RED);

		panel.add(new HorizontalBoxPanel(new FlowWrapper(FlowLayout.LEFT,
				statusPanel), Box.createHorizontalGlue(),
				new FlowWrapper(FlowLayout.RIGHT, new VericalBoxPanel(
						new JLabel(Win.logo), new FlowWrapper(FlowLayout.RIGHT,
								0, 0, new SwingLink("www.vitrulux.com",
										"http://www.vitrulux.com/"))))));

		add(panel, BorderLayout.PAGE_START);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

		JPanel buttonsPanel = new FlowWrapper(FlowLayout.RIGHT, startButton,
				zeroButton, zeroOkButton, stopButton);

		bottomPanel.add(new FlowWrapper(FlowLayout.LEFT, saveButton));
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(buttonsPanel);

		add(bottomPanel, BorderLayout.PAGE_END);

		motorX.addListener(motorListener);
		motorY.addListener(motorListener);

		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				int returnVal = chooser.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					if (file.exists()) {
						int resp = JOptionPane.showConfirmDialog(
								MainPanel.this,
								"File " + file.getAbsolutePath()
										+ " exists. Overwrite?", "Save",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (resp != JOptionPane.YES_OPTION)
							return;
					}
					doSave(file);
					// return true;
				} else {
					// return false;
				}

			}
		});

		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				status.setText(" ");
				if (thread != null)
					return;

				dataSet.clear();

				DIR primDir = DIR.valueOf(props.get(Prop.PRIMARY_DIR));
				final Float R = props.getFloat(Prop.SENSOR_DIST);

				thread = new ScanThread1(primDir, R, motorX, motorY,
						scanParamsX, scanParamsY, sensor, sensorPrams);
				thread.start();

				update();
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				status.setText(" ");
				try {
					motorX.stop();
				} catch (Exception e1) {
					status.setText(e1.getClass().getSimpleName() + " "
							+ e1.getMessage());
				}
				try {
					motorY.stop();
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
					motorX.zero();
					motorY.zero();
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
					motorX.zeroOK();
					motorY.zeroOK();
				} catch (Exception e1) {
					status.setText(e1.getClass().getSimpleName() + " "
							+ e1.getMessage());
				}
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				status.setText("");
			}
		});

		propsChanged();
	}

	public void propsChanged() {
		mainView.propsChanged();
		update();
	}

	private MotorListener motorListener = new MotorListener() {
		@Override
		public void onChanged() {
			update();
		}
	};

	private void update() {
		startButton.setEnabled(motorX.completed() && motorX.getPos() == 0
				&& motorY.completed() && motorY.getPos() == 0);
		int p = motorX.getPos();
		if (p < 0) {
			posDegX.setText("Азимутальный угол: Неизвестно");
		} else {
			float degX = scanParamsX.getDegStart() + p
					* (scanParamsX.getDegEnd() - scanParamsX.getDegStart())
					/ scanParamsX.getRange();
			posDegX.setText("Азимутальный угол: " + degX + " ("
					+ scanParamsX.getDegStart() + "..."
					+ scanParamsX.getDegEnd() + ")");
		}

		p = motorY.getPos();
		if (p < 0) {
			posDegY.setText("Полярный угол: Неизвестно");
		} else {
			float degY = scanParamsY.getDegStart() + p
					* (scanParamsY.getDegEnd() - scanParamsY.getDegStart())
					/ scanParamsY.getRange();
			posDegY.setText("Полярный угол: " + degY + " ("
					+ scanParamsY.getDegStart() + "..."
					+ scanParamsY.getDegEnd() + ")");
		}
	}

	private void doSave(File file) {
		try {
			PrintWriter wr = new PrintWriter(file);
			for (Data d : dataSet.getData()) {
				wr.print(d.x + "\t" + d.y + "\t" + d.R + "\t" + d.value.e
						+ "\t" + d.value.x + "\t" + d.value.y + "\t"
						+ d.value.t + "\t" + d.value.spectrum.size());
				for (Integer lambda : d.value.spectrum.keySet())
					wr.print("\t" + lambda + "\t"
							+ d.value.spectrum.get(lambda));
				wr.println();
			}
			wr.close();
			// document.save(file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"Error writing file " + file.getAbsolutePath());
		}
	}

}
