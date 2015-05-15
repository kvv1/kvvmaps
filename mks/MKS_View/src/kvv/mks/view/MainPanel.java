package kvv.mks.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import kvv.mks.cloud.Cloud;
import kvv.mks.view.draw.CloudDrawable;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private final List<CloudDrawable> clouds = new ArrayList<>();

	Point p;

	private static Color[] colors = { Color.GRAY, Color.GREEN, Color.RED,
			Color.BLUE };

	public MainPanel() {
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
