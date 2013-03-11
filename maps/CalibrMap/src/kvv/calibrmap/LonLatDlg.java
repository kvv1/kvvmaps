package kvv.calibrmap;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LonLatDlg extends JDialog {
	private static final long serialVersionUID = 1L;

	private LonLatPanel lonLatPanel = new LonLatPanel();
	private Point2D.Double lonLat;

	static class LonLatPanel {
		private static final long serialVersionUID = 1L;

		private JPanel panel = new JPanel();

		private JTextField lon = new JTextField(2);
		private JTextField lat = new JTextField(2);
		private JTextField lon1 = new JTextField(2);
		private JTextField lat1 = new JTextField(2);

		{
			panel.setLayout(new GridLayout(2, 5));
			panel.add(new JLabel("Долгота"));
			panel.add(lon);
			panel.add(new JLabel("град."));
			panel.add(lon1);
			panel.add(new JLabel("мин."));
			panel.add(new JLabel("Широта"));
			panel.add(lat);
			panel.add(new JLabel("град."));
			panel.add(lat1);
			panel.add(new JLabel("мин."));
		}

		public JPanel getPanel() {
			return panel;
		}

		public double getLon() {
			return Integer.parseInt(lon.getText())
					+ (lon1.getText().isEmpty() ? 0 : Integer.parseInt(lon1
							.getText()) / 60.0);
		}

		public double getLat() {
			return Integer.parseInt(lat.getText())
					+ (lat1.getText().isEmpty() ? 0 : Integer.parseInt(lat1
							.getText()) / 60.0);
		}

		public String getLonS() {
			return lon.getText() + " " + lon1.getText();
		}
		public String getLatS() {
			return lat.getText() + " " + lat1.getText();
		}

		public void setLonLat(double lon, double lat) {
			int lon1 = (int) (lon * 60 + 0.5);
			int lat1 = (int) (lat * 60 + 0.5);
			
			this.lon.setText("" + (lon1 / 60));
			this.lon1.setText("" + (lon1 % 60));
			
			this.lat.setText("" + (lat1 / 60));
			this.lat1.setText("" + (lat1 % 60));
		}
	}

	public LonLatDlg(JFrame parent, double lon, double lat) {
		super(parent, "Lon Lat", true);

		lonLatPanel.setLonLat(lon, lat);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new GridLayout(2, 1));
		getContentPane().add(lonLatPanel.getPanel());
		JPanel buttons = new JPanel(new FlowLayout());

		JButton okButton = new JButton("OK");
		buttons.add(okButton);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					lonLat = new Point2D.Double(lonLatPanel.getLon(),
							lonLatPanel.getLat());
				} catch (Exception ex) {
				}
				dispose();
			}
		});

		getContentPane().add(buttons);
		pack();
		setVisible(true);
	}
	
	public Point2D.Double getLonLat() {
		return lonLat;
	}

	public String getLonS() {
		return lonLatPanel.getLonS();
	}
	
	public String getLatS() {
		return lonLatPanel.getLatS();
	}
}
