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
	final MainPanel mainPanel;

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
					smsdX.close();
					smsdY.close();
					mainPanel.propsChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		propertiesPanel.add(new PropertyPanel(Prop.X_PORT,
				"���� ������������� ������"));
		propertiesPanel.add(new PropertyPanel(Prop.X_START_DEGREES,
				"��������� �������� ������������� ���� (��������)"));
		propertiesPanel.add(new PropertyPanel(Prop.X_END_DEGREES,
				"�������� �������� ������������� ���� (��������)"));
		propertiesPanel.add(new PropertyPanel(Prop.X_STEP_DEGREES,
				"��� ������������� ���� (��������)"));

		propertiesPanel.add(new PropertyPanel(Prop.Y_PORT,
				"���� ��������� ������"));
		propertiesPanel.add(new PropertyPanel(Prop.Y_START_DEGREES,
				"��������� �������� ��������� ���� (��������)"));
		propertiesPanel.add(new PropertyPanel(Prop.Y_END_DEGREES,
				"�������� �������� ��������� ���� (��������)"));
		propertiesPanel.add(new PropertyPanel(Prop.Y_STEP_DEGREES,
				"��� ��������� ���� (��������)"));

		propertiesPanel
				.add(new PropertyPanel(Prop.SENSOR_PORT, "���� �������"));

		propertiesPanel.addExt(new PropertyPanel(Prop.X_SPEED,
				"�������� ������������� ������ (�����/���.)"));
		propertiesPanel.addExt(new PropertyPanel(Prop.X_RANGE,
				"�������� ������������� ������ (�����)"));
		propertiesPanel.addExt(new PropertyPanel(Prop.Y_SPEED,
				"�������� ��������� ������ (�����/���.)"));
		propertiesPanel.addExt(new PropertyPanel(Prop.Y_RANGE,
				"�������� ��������� ������ (�����)"));
		propertiesPanel.addExt(new PropertyPanel(Prop.SENSOR_DELAY,
				"����� �������� ������� (��������)"));
		propertiesPanel.addExt(new PropertyPanel(Prop.AUTO_MOTOR_OFF,
				"��������� ������ ����� ������� ����������� (true/false)"));
		propertiesPanel.addExt(new PropertyPanel(Prop.MOTOR_ADDITIONAL_DELAY,
				"�������������� ����� ����� ��������� (ms)", "200"));
		propertiesPanel.addExt(new PropertyPanel(Prop.LAMBLA_BEGIN,
				"���. lambla (��)", "400"));
		propertiesPanel.addExt(new PropertyPanel(Prop.LAMBLA_END,
				"����. lambla (��)", "750"));
		propertiesPanel.addExt(new PropertyPanel(Prop.LAMBLA_STEP,
				"��� lambla (��)", "50"));
		propertiesPanel.addExt(new PropertyPanel(Prop.SENSOR_DIST,
				"���������� �� ������� (�)", "10"));

		propertiesPanel.load();

		if (propertiesPanel.properties.getProperty(Prop.DEBUG, "false").equals(
				"true")) {

			propertiesPanel.addExt(new PropertyPanel(Prop.SIM_MOTORS,
					"��������� ������� (true/false)"));
			propertiesPanel.addExt(new PropertyPanel(Prop.SIM_SENSOR,
					"��������� ������� (true/false)"));

			propertiesPanel.load();
		}

		Props props = new Props() {
			@Override
			public int getInt(String name, int defaultValue) {
				return Integer.parseInt(propertiesPanel.properties.getProperty(
						name, "" + defaultValue));
			}

			@Override
			public float getFloat(String name, float defaultValue) {
				return Float.parseFloat(propertiesPanel.properties.getProperty(
						name, "" + defaultValue));
			}

			@Override
			public String get(String name, String defaultValue) {
				return propertiesPanel.properties.getProperty(name,
						defaultValue);
			}
		};

		smsdX = new SMSD(props) {
			@Override
			protected String getPort() {
				return propertiesPanel.properties.getProperty(Prop.X_PORT, "");
			}

			@Override
			protected int getSpeed() {
				return Integer.parseInt(propertiesPanel.properties.getProperty(
						Prop.X_SPEED, "10000"));
			}
		};

		smsdY = new SMSD(props) {
			@Override
			protected String getPort() {
				return propertiesPanel.properties.getProperty(Prop.Y_PORT, "");
			}

			@Override
			protected int getSpeed() {
				return Integer.parseInt(propertiesPanel.properties.getProperty(
						Prop.Y_SPEED, "10000"));
			}
		};

		if (propertiesPanel.properties.getProperty(Prop.SIM_SENSOR, "false")
				.equals("true"))
			sensor = new TKA_VD_Sim();
		else
			sensor = new TKA_VD();

		mainPanel = new MainPanel(smsdX, smsdY, sensor, props);

		tabbedPane.add("�������", mainPanel);
		tabbedPane.add("������������", new ControlPanel(smsdX, smsdY, sensor,
				propertiesPanel));
		tabbedPane.add("���������", new FlowWrapper(FlowLayout.LEFT, 0, 0,
				propertiesPanel));

		// setSize(400, 500);
		propertiesPanel.onChanged();

		pack();
		setResizable(false);
		setTitle("���������");

		// setExtendedState(JFrame.MAXIMIZED_BOTH);
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
