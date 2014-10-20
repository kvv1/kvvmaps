package kvv.goniometer.ui.mainpage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import kvv.goniometer.SensorData;

@SuppressWarnings("serial")
public class SpectrumView extends JPanel {

	@Override
	public void paint(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (sensorData == null) {
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, getWidth(), getHeight());
			return;
		}

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		int minLambda = Integer.MAX_VALUE;
		int maxLambda = Integer.MIN_VALUE;
		int maxValue = 0;

		for (Integer lambda : sensorData.spectrum.keySet()) {
			minLambda = Math.min(minLambda, lambda);
			maxLambda = Math.max(maxLambda, lambda);
			maxValue = Math.max(maxValue, sensorData.spectrum.get(lambda));
		}

		g.setColor(Color.DARK_GRAY);

		for (Integer lambda : sensorData.spectrum.keySet()) {
			int value = sensorData.spectrum.get(lambda);

			int x = (lambda - minLambda) * getWidth() / (maxLambda - minLambda);
			
			int h = getHeight();
			int y = value * h / maxValue;

			oval(g, x, h - y, 4);

		}

	}

	SensorData sensorData;

	public void setData(SensorData sensorData) {
		this.sensorData = sensorData;
		repaint();
	}

	private static void oval(Graphics2D g, int x, int y, int r) {
		g.fillOval(x - r / 2, y - r / 2, r, r);
	}

}
