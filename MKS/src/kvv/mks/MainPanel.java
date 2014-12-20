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
import kvv.mks.cloud.Pt;
import kvv.mks.draw.CloudDrawable;
import kvv.mks.rot.M;
import kvv.mks.rot.Rot;
import kvv.mks.rot.quaternion.Quaternion;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private final List<CloudDrawable> clouds = new ArrayList<>();

	Point p;

	private static Color[] colors = { Color.GRAY, Color.GREEN, Color.RED,
			Color.BLUE };

	private Rot matrix = M.instance.create();

	public static List<Rot> directions = new ArrayList<>();

	static {

			Rot qq = Quaternion.rot(0, Util.g2r(20), 0);
		
			Rot q;
			q = Quaternion.fromDir(new Pt(0, 1, 1)).mul(qq, null);
			directions.add(q);
			
			q = Quaternion.fromDir(new Pt(0, 1, 0)).mul(qq, null);
			directions.add(q);
			
			q = Quaternion.fromDir(new Pt(0, 1, -1)).mul(qq, null);
			directions.add(q);
			
			q = Quaternion.fromDir(new Pt(0, 0, -1)).mul(qq, null);
			directions.add(q);
			
			q = Quaternion.fromDir(new Pt(0, -1, -1)).mul(qq, null);
			directions.add(q);
			
			q = Quaternion.fromDir(new Pt(0, -1, 0)).mul(qq, null);
			directions.add(q);
			
			q = Quaternion.fromDir(new Pt(0, -1, 1)).mul(qq, null);
			directions.add(q);
			
			q = Quaternion.fromDir(new Pt(0, 0, 1)).mul(qq, null);
			directions.add(q);
			
			
			
/*
		for (int x : new int[] { -1, 1 })
			for (int y : new int[] { -1, 1 })
				for (int z : new int[] { -1, 1 })
					// for (int x : new int[] { -1, 0, 1 })
					// for (int y : new int[] { -1, 0, 1 })
					// for (int z : new int[] { -1, 0, 1 })
					if (x != 0 || y != 0 || z != 0)
						for (int a = 0; a < 180; a += 15) {
							// directions[n++] = Quaternion.fromDir(new Pt(x, y,
							// z));
							double cos = Math.cos(Util.g2r(a) / 2);
							double sin = Math.sin(Util.g2r(a) / 2);
							directions.add(new Quaternion(cos, x * sin,
									y * sin, z * sin).unit(null));
						}
*/						
	}

	int n = 0;

	private void nextDir() {
		matrix = directions.get(n++);
		setMatrix(matrix);
		if (n == directions.size())
			n=0;
		repaint();
	}

	public MainPanel() {

		JButton zero = new JButton("0");
		zero.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				matrix = M.instance.create();
				setMatrix(matrix);
				repaint();
			}
		});
		add(zero);

		JButton next = new JButton("next");
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextDir();
			}
		});
		add(next);

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
				setMatrix(matrix);
				p = e.getPoint();
				repaint();
			}
		});

		// pcd.rotate(0, 0, 0.1);
	}

	public void setMatrix(Rot matrix) {
		for (CloudDrawable drawable : clouds)
			drawable.setMatrix(matrix);
	}

	@Override
	public void paintComponent(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;

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
