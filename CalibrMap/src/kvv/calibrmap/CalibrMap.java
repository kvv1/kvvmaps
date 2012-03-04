package kvv.calibrmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CalibrMap extends JFrame {
	private static final long serialVersionUID = 1L;

	class CalPt {
		public CalPt(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int x;
		public int y;
		public String lonS;
		public String latS;
		public double lon;
		public double lat;
	}

	private CalPt[] points = new CalPt[4];
	private CalPt curPt;
	private BufferedImage img;
	private String fileName;
	private double lon0;
	private double lon1;
	private double lat0;
	private double lat1;

	private File dir;

	private final JPanel panel = new JPanel() {
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g) {
			if (img == null)
				return;
			g.drawImage(img, 0, 0, null);
			g.setColor(Color.RED);
			for (CalPt p : points) {
				if (p != null) {
					if (p == curPt)
						g.fillRect(p.x - 4, p.y - 4, 8, 8);
					else
						g.drawRect(p.x - 4, p.y - 4, 8, 8);

					g.drawString(String.format("x %d  y %d", p.x, p.y),
							p.x + 10, p.y + 10);
					if (p.lonS != null && p.latS != null) {
						g.drawString(
								String.format("lon %s  lat %s", p.lonS, p.latS),
								p.x + 10, p.y + 20);
					}
				}

			}
		}
	};

	private final JScrollPane scrollPane = new JScrollPane(panel);

	private CalibrMap() throws IOException {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int w = panel.getWidth();
				int h = panel.getHeight();
				int x = e.getX();
				int y = e.getY();

				int idx = (x < w / 2 ? 0 : 1) + (y < h / 2 ? 0 : 2);
				CalPt curPt = new CalPt(x, y);
				if (idx == 0 || idx == 3) {
					LonLatDlg dlg = new LonLatDlg(CalibrMap.this,
							idx == 0 ? lon0 : lon1, idx == 0 ? lat0 : lat1);
					Point2D.Double lonLat = dlg.getLonLat();
					if (lonLat == null)
						return;
					curPt.lon = lonLat.x;
					curPt.lat = lonLat.y;
					curPt.lonS = dlg.getLonS();
					curPt.latS = dlg.getLatS();
				}

				points[idx] = curPt;
				CalibrMap.this.curPt = curPt;

				panel.repaint();
			}
		});

		JPanel toolbar = new JPanel(new FlowLayout());

		JButton openButton = new JButton("Открыть");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		toolbar.add(openButton);

		JButton saveButton = new JButton("Сохранить");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (img == null)
					return;

				for (CalPt p : points)
					if (p == null) {
						JOptionPane
								.showMessageDialog(CalibrMap.this,
										"Необходимо указать четыре калибровочные точки");
						return;
					}
				PrintWriter wr;
				try {
					wr = new PrintWriter(fileName.substring(0,
							fileName.lastIndexOf('.'))
							+ ".cal");

					wr.printf("img=%s\n", new File(fileName).getName());
					wr.printf("point1=%d,%d,%g,%g\n", points[0].x, points[0].y,
							points[0].lon, points[0].lat);
					wr.printf("point2=%d,%d,%g,%g\n", points[1].x, points[1].y,
							points[3].lon, points[0].lat);
					wr.printf("point3=%d,%d,%g,%g\n", points[3].x, points[3].y,
							points[3].lon, points[3].lat);
					wr.printf("point4=%d,%d,%g,%g\n", points[2].x, points[2].y,
							points[0].lon, points[3].lat);

					wr.close();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(CalibrMap.this,
							"Ошибка записи");
					e1.printStackTrace();
				}
			}
		});
		toolbar.add(saveButton);

		setLayout(new BorderLayout());

		add(toolbar, BorderLayout.NORTH);
		add(scrollPane);

		setSize(1200, 900);
		setLocation(0, 0);
		setVisible(true);
	}

	private void open() {
		JFileChooser fileopen = new JFileChooser(dir);
		int ret = fileopen.showDialog(null, "Открыть файл");
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fileopen.getSelectedFile();
			dir = file.getParentFile();
			try {
				img = ImageIO.read(file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (img == null) {
				JOptionPane.showMessageDialog(CalibrMap.this,
						"Неизвестный формат изображеия");
				return;
			}

			fileName = file.getAbsolutePath();
			panel.setPreferredSize(new Dimension(img.getWidth(), img
					.getHeight()));
			scrollPane.revalidate();
			setTitle(fileName);
			points[0] = points[1] = points[2] = points[3] = null;
			CalibrMap.this.repaint();

			this.lon0 = 0;
			this.lon1 = 0;
			this.lat0 = 0;
			this.lat1 = 0;

			String name = new File(fileName).getName().split("\\.")[0];
			if (name.matches("[a-zA-Z]-\\d\\d-\\d\\d\\d")) {
				String[] parts = name.split("-");
				double lat0 = toLat(parts[0].charAt(0));
				double lon0 = toLon(Integer.parseInt(parts[1]));
				int part = Integer.parseInt(parts[2]);
				setBounds(lon0, lat0, part - 1, 12, 12);
			} else if (name.matches("[a-zA-Z]-\\d\\d-\\d\\d\\d-\\d\\d\\d")) {
				String[] parts = name.split("-");
				double lat0 = toLat(parts[0].charAt(0));
				double lon0 = toLon(Integer.parseInt(parts[1]));
				int part = Integer.parseInt(parts[2]);
				int part2 = Integer.parseInt(parts[3]);
				if (part2 == part + 1)
					setBounds(lon0, lat0, (part - 1) / 2, 6, 12);
			} else if (name.matches("[a-zA-Z]-\\d\\d-[ABVGabvg]")) {
				String[] parts = name.split("-");
				double lat0 = toLat(parts[0].charAt(0));
				double lon0 = toLon(Integer.parseInt(parts[1]));
				int part = abvg2idx(parts[2].charAt(0));
				setBounds(lon0, lat0, part - 1, 2, 2);
			} else if (name.matches("[a-zA-Z]\\d\\d-\\d\\d")) {
				String[] parts = name.split("-");
				double lat0 = toLat(parts[0].charAt(0));
				double lon0 = toLon(Integer.parseInt(parts[0].substring(1, 3)));
				int part = Integer.parseInt(parts[1]);
				setBounds(lon0, lat0, part - 1, 6, 6);
			} else if (name.matches("[a-zA-Z]-\\d\\d-\\d\\d")) {
				String[] parts = name.split("-");
				double lat0 = toLat(parts[0].charAt(0));
				double lon0 = toLon(Integer.parseInt(parts[1]));
				int part = Integer.parseInt(parts[2]);
				setBounds(lon0, lat0, part - 1, 6, 6);
			} else if (name.matches("[a-zA-Z]-\\d\\d-\\d\\d-\\d\\d")) {
				String[] parts = name.split("-");
				double lat0 = toLat(parts[0].charAt(0));
				double lon0 = toLon(Integer.parseInt(parts[1]));
				int part = Integer.parseInt(parts[2]);
				int part2 = Integer.parseInt(parts[3]);
				if (part2 == part + 1)
					setBounds(lon0, lat0, (part - 1) / 2, 3, 6);
			}
		}
	}

	void setBounds(double lon0, double lat0, int part, int partsPerLine,
			int partsPerCol) {
		lon0 += part % partsPerLine / (partsPerLine / 6.0);
		lat0 -= part / partsPerLine / (partsPerCol / 4.0);
		this.lon0 = lon0;
		this.lon1 = lon0 + 6.0 / partsPerLine;
		this.lat0 = lat0;
		this.lat1 = lat0 - 4.0 / partsPerCol;
	}

	private int abvg2idx(char c) {
		c = Character.toLowerCase(c);
		if (c == 'a')
			return 0;
		if (c == 'b')
			return 1;
		if (c == 'v')
			return 2;
		if (c == 'g')
			return 3;
		throw new IllegalArgumentException("letter = " + c);
	}

	private int toLat(char ch) {
		int n = Character.toLowerCase(ch) - 'a' + 1;
		n *= 4;
		return n;
	}

	private int toLon(int n) {
		n *= 6;
		n -= 180;
		n -= 6;
		return n;
	}

	public static void main(String[] args) throws IOException {
		new CalibrMap();
	}

}
