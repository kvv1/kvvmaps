package kvv.mks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import kvv.mks.cloud.Cloud;
import kvv.mks.draw.CloudDrawable;
import kvv.mks.rot.M;
import kvv.mks.rot.Rot;
import kvv.mks.rot.matrix.Matrix3x3;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private final List<CloudDrawable> clouds = new ArrayList<>();

	Point p;

	private static Color[] colors = { Color.GRAY, Color.GREEN, Color.RED,
			Color.BLUE };

	private Rot matrix = M.instance.create();

	public MainPanel() {

		JButton zero = new JButton("0");
		zero.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				matrix = M.instance.create();
				for (CloudDrawable drawable : clouds)
					drawable.setMatrix(matrix);
				repaint();
			}
		});
		add(zero);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// pcdDr.rotate();
				// repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				p = e.getPoint();
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				Point p1 = e.getPoint();
				matrix = M.instance.rot(-(p1.y - p.y) / 100.0, 0, 0).mul(
						matrix, null);
				matrix = M.instance.rot(0, (p1.x - p.x) / 100.0, 0).mul(matrix,
						null);
				for (CloudDrawable drawable : clouds)
					drawable.setMatrix(matrix);
				p = e.getPoint();
				repaint();
			}
		});

		// pcd.rotate(0, 0, 0.1);
	}

	@Override
	public void paint(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;
		super.paint(g);

		int w = getWidth();
		int h = getHeight();

		for (CloudDrawable drawable : clouds)
			drawable.draw(g, w, h);
	}

	public void add(Cloud cloud) {
		clouds.add(new CloudDrawable(cloud, colors[clouds.size()]));
		repaint();
	}

}
