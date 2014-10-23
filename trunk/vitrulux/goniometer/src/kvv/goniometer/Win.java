package kvv.goniometer;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import kvv.goniometer.hw.motor.MotorSim;
import kvv.goniometer.hw.motor.SMSD;
import kvv.goniometer.hw.sensor.TKA_VD;
import kvv.goniometer.hw.sensor.TKA_VD_Sim;
import kvv.goniometer.ui.mainpage.MainPanel;
import kvv.goniometer.ui.props.Prop;
import kvv.goniometer.ui.props.PropertiesPanel;
import kvv.goniometer.ui.props.PropertyPanel;
import kvv.goniometer.ui.utils.FlowWrapper;

@SuppressWarnings("serial")
public class Win extends JFrame {

	JTabbedPane tabbedPane;

	Motor smsdX;
	Motor smsdY;
	Sensor sensor;

	Thread thread;

	public static ImageIcon logo;

	public Win() {
		try {

			logo = new ImageIcon(ImageIO.read(getClass().getResource(
					"vitrulux.png")));
		} catch (IOException e) {
		}

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				try {
					smsdX.stop();
				} catch (Exception e) {
				}
				try {
					smsdY.stop();
				} catch (Exception e) {
				}
				System.exit(0);
			}
		});

		tabbedPane = new JTabbedPane() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isFocusable() {
				return false;
			}
		};

		getContentPane().add(tabbedPane);

		final PropertiesPanel propertiesPanel = new PropertiesPanel() {
			@Override
			public void onChanged() {
				try {
					sensor.init(properties.getProperty(Prop.SENSOR_PORT, ""));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		propertiesPanel.add(new PropertyPanel(Prop.X_PORT,
				"���� ������������� ������"));
		propertiesPanel.addExt(new PropertyPanel(Prop.X_SPEED,
				"�������� ������������� ������ (�����/���.)"));
		propertiesPanel.addExt(new PropertyPanel(Prop.X_RANGE,
				"�������� ������������� ������ (�����)"));
		propertiesPanel.add(new PropertyPanel(Prop.X_START_DEGREES,
				"��������� �������� ������������� ���� (��������)"));
		propertiesPanel.add(new PropertyPanel(Prop.X_END_DEGREES,
				"�������� �������� ������������� ���� (��������)"));
		propertiesPanel.add(new PropertyPanel(Prop.X_STEP_DEGREES,
				"��� ������������� ���� (��������)"));

		propertiesPanel.add(new PropertyPanel(Prop.Y_PORT,
				"���� ��������� ������"));
		propertiesPanel.addExt(new PropertyPanel(Prop.Y_SPEED,
				"�������� ��������� ������ (�����/���.)"));
		propertiesPanel.addExt(new PropertyPanel(Prop.Y_RANGE,
				"�������� ��������� ������ (�����)"));
		propertiesPanel.add(new PropertyPanel(Prop.Y_START_DEGREES,
				"��������� �������� ��������� ���� (��������)"));
		propertiesPanel.add(new PropertyPanel(Prop.Y_END_DEGREES,
				"�������� �������� ��������� ���� (��������)"));
		propertiesPanel.add(new PropertyPanel(Prop.Y_STEP_DEGREES,
				"��� ��������� ���� (��������)"));

		propertiesPanel
				.add(new PropertyPanel(Prop.SENSOR_PORT, "���� �������"));
		propertiesPanel.addExt(new PropertyPanel(Prop.SENSOR_DELAY,
				"����� �������� ������� (��������)"));

		propertiesPanel.addExt(new PropertyPanel(Prop.AUTO_MOTOR_OFF,
				"��������� ������ ����� ������� ����������� (true/false)"));

		propertiesPanel.addExt(new PropertyPanel(Prop.MOTOR_ADDITIONAL_DELAY,
				"�������������� ����� ����� ��������� (ms)", "200"));

		propertiesPanel.load();

		if (propertiesPanel.properties.getProperty(Prop.DEBUG, "false").equals(
				"true")) {

			propertiesPanel.addExt(new PropertyPanel(Prop.SIM_MOTORS,
					"��������� ������� (true/false)"));
			propertiesPanel.addExt(new PropertyPanel(Prop.SIM_SENSOR,
					"��������� ������� (true/false)"));

			propertiesPanel.load();
		}

//		if (propertiesPanel.properties.getProperty(Prop.SIM_MOTORS, "false")
//				.equals("true")) {
//			smsdX = new MotorSim();
//			smsdY = new MotorSim();
//		} else {
			smsdX = new SMSD() {
				@Override
				protected String getPort() {
					return propertiesPanel.properties.getProperty(Prop.X_PORT,
							"");
				}

				@Override
				protected int getSpeed() {
					return Integer.parseInt(propertiesPanel.properties
							.getProperty(Prop.X_SPEED, "10000"));
				}

				@Override
				protected boolean isAutoOff() {
					return propertiesPanel.properties.getProperty(
							Prop.AUTO_MOTOR_OFF, "false").equals("true");
				}

				@Override
				protected int getAdditioalDelay() {
					String s = propertiesPanel.properties
							.getProperty(Prop.MOTOR_ADDITIONAL_DELAY);
					return Integer.parseInt(s);
				}

				@Override
				protected boolean isSim() {
					return propertiesPanel.properties.getProperty(
							Prop.SIM_MOTORS, "false").equals("true");
				}
			};

			smsdY = new SMSD() {
				@Override
				protected String getPort() {
					return propertiesPanel.properties.getProperty(Prop.Y_PORT,
							"");
				}

				@Override
				protected int getSpeed() {
					return Integer.parseInt(propertiesPanel.properties
							.getProperty(Prop.Y_SPEED, "10000"));
				}

				@Override
				protected boolean isAutoOff() {
					return propertiesPanel.properties.getProperty(
							Prop.AUTO_MOTOR_OFF, "false").equals("true");
				}

				@Override
				protected int getAdditioalDelay() {
					return Integer.parseInt(propertiesPanel.properties
							.getProperty(Prop.MOTOR_ADDITIONAL_DELAY));
				}

				@Override
				protected boolean isSim() {
					return propertiesPanel.properties.getProperty(
							Prop.SIM_MOTORS, "false").equals("true");
				}
			};
//		}

		if (propertiesPanel.properties.getProperty(Prop.SIM_SENSOR, "false")
				.equals("true"))
			sensor = new TKA_VD_Sim();
		else
			sensor = new TKA_VD();

		propertiesPanel.onChanged();

		MainPanel mainPanel = new MainPanel(smsdX, smsdY, sensor) {

			@Override
			protected int getRangeX() {
				return Integer.parseInt(propertiesPanel.properties.getProperty(
						Prop.X_RANGE, "0"));
			}

			@Override
			protected float getDegStartX() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.X_START_DEGREES, "0"));
			}

			@Override
			protected float getDegEndX() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.X_END_DEGREES, "0"));
			}

			@Override
			protected float getDegStepX() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.X_STEP_DEGREES, "0"));
			}

			@Override
			protected int getRangeY() {
				return Integer.parseInt(propertiesPanel.properties.getProperty(
						Prop.Y_RANGE, "0"));
			}

			@Override
			protected float getDegStartY() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.Y_START_DEGREES, "0"));
			}

			@Override
			protected float getDegEndY() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.Y_END_DEGREES, "0"));
			}

			@Override
			protected float getDegStepY() {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						Prop.Y_STEP_DEGREES, "0"));
			}

			@Override
			protected int getSensorDelay() {
				return Integer.parseInt(propertiesPanel.properties.getProperty(
						Prop.SENSOR_DELAY, "0"));
			}

		};

		tabbedPane.add("�������", mainPanel);
		tabbedPane.add("������������", new ControlPanel(smsdX, smsdY, sensor,
				propertiesPanel));
		tabbedPane.add("���������", new FlowWrapper(FlowLayout.LEFT, 0, 0,
				propertiesPanel));

		// setSize(400, 500);
		pack();
		setResizable(false);
		setTitle("���������");
		setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Win();
			}
		});
	}
}
