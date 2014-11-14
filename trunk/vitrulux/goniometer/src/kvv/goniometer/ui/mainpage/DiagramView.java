package kvv.goniometer.ui.mainpage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import kvv.goniometer.ui.mainpage.DataSet.Data;

@SuppressWarnings("serial")
public abstract class DiagramView extends JPanel {
	private final DataSet dataSet;

	private final Color c = Color.LIGHT_GRAY;
	private final Color c1 = new Color(c.getRed(), c.getGreen(), c.getBlue(),
			128);

	protected abstract DIR getDir();

	protected abstract float getPrim();

	public DiagramView(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	private static void oval(Graphics2D g, int x, int y, int r) {
		g.fillOval(x - r / 2, y - r / 2, r, r);
	}

	public void propsChanged() {
	}

	@Override
	public void paint(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		int R = getWidth() / 2 - 22;

		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;

		g.setColor(Color.WHITE);
		for (int a = 0; a < 360; a += 10) {
			double sin = Math.sin(a * Math.PI / 180);
			double cos = Math.cos(a * Math.PI / 180);
			g.drawLine(centerX, centerY, centerX + (int) (R * cos), centerY
					+ (int) (R * sin));
		}

		g.drawOval(centerX - R, centerY - R, 2 * R, 2 * R);
		g.drawOval(centerX - R * 3 / 4, centerY - R * 3 / 4, R * 3 / 2,
				R * 3 / 2);
		g.drawOval(centerX - R / 2, centerY - R / 2, R, R);
		g.drawOval(centerX - R / 4, centerY - R / 4, R / 2, R / 2);

		g.setColor(Color.LIGHT_GRAY);
		g.fillOval(centerX - 10, centerY - 10, 20, 20);

		int maxValue = 1;
		for (Data d : dataSet.getData()) {
			float v = d.getPrim(getDir());
			if (v == getPrim() && d.value.e > maxValue)
				maxValue = d.value.e;
		}

		Point old = null;

		g.setStroke(new BasicStroke(2));

		g.setColor(new Color(0x008080));
		for (Data d : dataSet.getData()) {
			float v = d.getPrim(getDir());
			if (v != getPrim())
				continue;

			float a = d.getSec(getDir());
			double sin = Math.sin(a * Math.PI / 180);
			double cos = Math.cos(a * Math.PI / 180);
			int x = centerX + (int) (d.value.e * R * cos / maxValue);
			int y = centerY - (int) (d.value.e * R * sin / maxValue);
			oval(g, x, y, 4);

			if (old != null)
				g.drawLine(old.x, old.y, x, y);

			old = new Point(x, y);

		}

		for (int a = -180 + 30; a <= 180; a += 30) {
			int r = getWidth() / 2 - 10;
			double sin = Math.sin(a * Math.PI / 180);
			double cos = Math.cos(a * Math.PI / 180);
			int x = centerX + (int) (r * cos);
			int y = centerY - (int) (r * sin);
			String txt = "" + a;

			int w = g.getFontMetrics().stringWidth(txt);
			int h = g.getFontMetrics().getHeight();
			drawText(g, txt, x - w / 2, y - h / 2);
		}

		drawText(g,
				getDir() == DIR.AZIMUTH ? DIR.POLAR.text : DIR.AZIMUTH.text, 4,
				4);

	}

	private void drawText(Graphics2D g, String txt, int x, int y) {
		int w = g.getFontMetrics().stringWidth(txt);
		int h = g.getFontMetrics().getHeight();
		int descent = g.getFontMetrics().getDescent();
		int leading = g.getFontMetrics().getLeading();

		g.setColor(c1);
		g.fillRect(x, y - (-leading - descent) / 2, w, (h - leading - descent));
		g.setColor(Color.BLACK);
		g.drawString(txt, x, y + h - descent);
	}

	public void updateData() {
		repaint();
	}

}
