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
import kvv.goniometer.Sensor.SensorListener;
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

	public MainPanel(final Motor motorX, final Motor motorY, Sensor sensor,
			Props props) {
		this.motorX = motorX;
		this.motorY = motorY;
		this.sensor = sensor;
		this.props = props;

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

				thread = new T();
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

		paramsChanged();
	}

	public void paramsChanged() {
		mainView.setParams();
		update();
	}

	private MotorListener motorListener = new MotorListener() {
		@Override
		public void onChanged() {
			update();
		}
	};

	private int getRangeX() {
		return props.getInt(Prop.X_RANGE, 0);
	}

	private float getDegStartX() {
		return props.getFloat(Prop.X_START_DEGREES, 0);
	}

	private float getDegEndX() {
		return props.getFloat(Prop.X_END_DEGREES, 0);
	}

	private float getDegStepX() {
		return props.getFloat(Prop.X_STEP_DEGREES, 0);
	}

	private int getRangeY() {
		return props.getInt(Prop.Y_RANGE, 0);
	}

	private float getDegStartY() {
		return props.getFloat(Prop.Y_START_DEGREES, 0);
	}

	private float getDegEndY() {
		return props.getFloat(Prop.Y_END_DEGREES, 0);
	}

	private float getDegStepY() {
		return props.getFloat(Prop.Y_STEP_DEGREES, 0);
	}

	private int getSensorDelay() {
		return props.getInt(Prop.SENSOR_DELAY, 0);
	}

	private void update() {
		startButton.setEnabled(motorX.completed() && motorX.getPos() == 0
				&& motorY.completed() && motorY.getPos() == 0);
		int p = motorX.getPos();
		if (p < 0) {
			posDegX.setText("Азимутальный угол: Неизвестно");
		} else {
			float degX = getDegStartX() + p * (getDegEndX() - getDegStartX())
					/ getRangeX();
			posDegX.setText("Азимутальный угол: " + degX + " ("
					+ getDegStartX() + "..." + getDegEndX() + ")");
		}

		p = motorY.getPos();
		if (p < 0) {
			posDegY.setText("Полярный угол: Неизвестно");
		} else {
			float degY = getDegStartY() + p * (getDegEndY() - getDegStartY())
					/ getRangeY();
			posDegY.setText("Полярный угол: " + degY + " (" + getDegStartY()
					+ "..." + getDegEndY() + ")");
		}
	}

	private volatile T thread;

	class T extends Thread {
		int cnt;
		volatile SensorData data;

		@Override
		public void run() {
			SensorListener listener = new SensorListener() {
				@Override
				public synchronized void onChanged(SensorData data) {
					if (cnt < getSensorDelay()) {
						cnt++;
					} else {
						T.this.data = data;
					}
				}
			};

			sensor.addListener(listener);

			try {
				for (float y = getDegStartY(); y <= getDegEndY(); y += getDegStepY()) {
					new Interruptor() {
						@Override
						protected boolean readyToContinue() {
							return motorY.completed();
						}
					}.pause();
					motorY.moveTo((int) ((y - getDegStartY()) * getRangeY() / (getDegEndY() - getDegStartY())));
					for (float x = getDegStartX(); x <= getDegEndX(); x += getDegStepX()) {
						new Interruptor() {
							@Override
							protected boolean readyToContinue() {
								return motorX.completed();
							}
						}.pause();
						motorX.moveTo((int) ((x - getDegStartX()) * getRangeX() / (getDegEndX() - getDegStartX())));
						new Interruptor() {
							@Override
							protected boolean readyToContinue() {
								return motorY.completed() && motorX.completed();
							}
						}.pause();

						synchronized (listener) {
							cnt = 0;
							data = null;
						}

						new Interruptor() {
							@Override
							protected boolean readyToContinue() {
								return data != null;
							}
						}.pause();

						final float x1 = x;
						final float y1 = y;
						final SensorData d1 = data;

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dataSet.addMeasure(x1, y1, d1);
							}
						});

					}
				}

				motorX.moveTo(0);
				motorY.moveTo(0);

			} catch (Exception e) {
				status.setText(e.getClass().getSimpleName() + " "
						+ e.getMessage());
			} finally {
				try {
					new Interruptor() {
						@Override
						protected boolean readyToContinue() {
							return motorY.completed() && motorX.completed();
						}
					}.pause();
					try {
						motorX.onOff(false);
					} catch (Exception e) {
					}
					try {
						motorY.onOff(false);
					} catch (Exception e) {
					}
				} catch (InterruptedException e1) {
					status.setText(e1.getClass().getSimpleName() + " "
							+ e1.getMessage());
				}
				sensor.removeListener(listener);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						thread = null;
						update();
					}
				});
			}
		}
	}

	abstract class Interruptor {
		protected abstract boolean readyToContinue();

		public void pause() throws InterruptedException {
			do {
				if (motorX.getPos() < 0 || motorY.getPos() < 0)
					throw new InterruptedException("операция прервана");
			} while (!readyToContinue());
		}

	}

	private void doSave(File file) {
		try {
			PrintWriter wr = new PrintWriter(file);
			for (Data d : dataSet.getData()) {
				wr.print(d.x + "\t" + d.y + "\t" + d.value.e + "\t" + d.value.x
						+ "\t" + d.value.y + "\t" + d.value.t + "\t"
						+ d.value.spectrum.size());
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
