package kvv.goniometer.ui.mainpage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JPanel;

import kvv.goniometer.ui.mainpage.DataSet.Data;

@SuppressWarnings("serial")
public class DiagramView extends JPanel implements IMainView {
	private final DataSet dataSet;
	private float polar;

	public DiagramView(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	public void setPolar(float polar) {
		this.polar = polar;
		repaint();
	}

	private static void oval(Graphics2D g, int x, int y, int r) {
		g.fillOval(x - r / 2, y - r / 2, r, r);
	}

	@Override
	public void setParams(float minX, float maxX, float stepX, float minY,
			float maxY, float stepY) {
	}

	@Override
	public void paint(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
		        RenderingHints.VALUE_ANTIALIAS_ON);
		

		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;

		g.setColor(Color.WHITE);
		for (int a = 0; a < 360; a += 10) {
			double sin = Math.sin(a * Math.PI / 180);
			double cos = Math.cos(a * Math.PI / 180);
			g.drawLine(centerX, centerY, centerX
					+ (int) ((getWidth() + getHeight()) * cos), centerY
					+ (int) ((getWidth() + getHeight()) * sin));
		}

		int maxValue = 1;
		for (Data d : dataSet.getData()) {
			if (d.y == polar && d.value.e > maxValue)
				maxValue = d.value.e;
		}

		int r = getHeight() / 2;

		g.setColor(Color.DARK_GRAY);
		for (Data d : dataSet.getData()) {
			if (d.y != polar)
				continue;

			float a = d.x;
			double sin = Math.sin(a * Math.PI / 180);
			double cos = Math.cos(a * Math.PI / 180);
			oval(g, centerX + (int) (d.value.e * r / maxValue * cos), centerY
					- (int) (d.value.e * r / maxValue * sin), 4);
		}
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void updateData(Float polar) {
		if (polar == null || polar == this.polar)
			repaint();
	}

}
