package kvv.goniometer.ui.mainpage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JPanel;

import kvv.goniometer.ui.mainpage.DataSet.Data;

@SuppressWarnings("serial")
public class DataCanvas extends JPanel implements IMainView{

	private static final int WIDTH = 360;
	private static final int HEIGHT = 360;

	private final DataSet dataSet;

	private float minX;
	private float maxX;
	private float stepX;
	private float minY;
	private float maxY;
	private float stepY;

	public DataCanvas(DataSet dataSet) {
		this.dataSet = dataSet;
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
	}

	@Override
	public void setParams(float minX, float maxX, float stepX, float minY, float maxY,
			float stepY) {
		this.minX = minX;
		this.maxX = maxX;
		this.stepX = stepX;
		this.minY = minY;
		this.maxY = maxY;
		this.stepY = stepY;
	}

	@Override
	public void paint(Graphics _g) {
		// _g.drawImage(image, 0, 0, null);
		Graphics2D g = (Graphics2D) _g;

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		int maxValue = 1;
		for (Data d : dataSet.getData()) {
			if (d.value.e > maxValue)
				maxValue = d.value.e;
		}

		for (Data d : dataSet.getData()) {
			int x1 = (int) ((d.x - minX) * WIDTH / (maxX + stepX - minX));
			if (x1 < 0)
				x1 = 0;

			int y1 = (int) ((d.y - minY) * HEIGHT / (maxY + stepY - minY));
			if (y1 < 0)
				y1 = 0;

			int w = (int) (stepX * WIDTH / (maxX + stepX - minX));
			int h = (int) (stepY * HEIGHT / (maxY + stepY - minY));

			int n = d.value.e * 255 / maxValue;

			g.setColor(new Color(n, n, n));

			g.fillRect(x1, y1, w, h);
		}

	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void updateData(Float polar) {
		repaint();
	}
}
