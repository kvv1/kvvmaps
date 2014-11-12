package kvv.goniometer.ui.mainpage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import kvv.goniometer.Props;
import kvv.goniometer.SensorData;
import kvv.goniometer.ui.props.Prop;

@SuppressWarnings("serial")
public class SpectrumView extends JPanel {

	private static final int LAMBDA_PANEL_HEIGHT = 20;

	private final Props props;

	public SpectrumView(Props props) {
		this.props = props;
	}

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

		int h = getHeight() - LAMBDA_PANEL_HEIGHT;

		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(0, h, getWidth(), h);

		for (int lambda = props.getInt(Prop.LAMBLA_BEGIN, 0); lambda <= props
				.getInt(Prop.LAMBLA_END, 0); lambda += props.getInt(
				Prop.LAMBLA_STEP, 1)) {

			int x = (lambda - minLambda) * getWidth() / (maxLambda - minLambda);

			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(x, 0, x, h);

			String txt = "" + lambda;

			int w = g.getFontMetrics().stringWidth(txt);
			g.setColor(Color.DARK_GRAY);
			g.drawString(txt, x - w / 2, getHeight() - 4);
		}

		g.setColor(Color.LIGHT_GRAY);
		for (int i = 0; i < 100; i += 25) {
			int y = h - (h * i / 100);
			g.drawLine(0, y, getWidth(), y);
		}

		g.setStroke(new BasicStroke(2));

		Point prev = null;
		int prevLambda = 0;

		for (Integer lambda : sensorData.spectrum.keySet()) {
			int value = sensorData.spectrum.get(lambda);

			int x = (lambda - minLambda) * getWidth() / (maxLambda - minLambda);

			int y = value * h / maxValue;


			if (prev != null) {
				g.setColor(wvColor((lambda + prevLambda) / 2, 1f));
				g.drawLine(prev.x, h - prev.y, x, h - y);
				prevLambda = lambda;
			}

			g.setColor(wvColor(lambda, 1f));
			oval(g, x, h - y, 4);
			
			prev = new Point(x, y);
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

	public static Color wvColor(float wl, float gamma) {
		/**
		 * red, green, blue component in range 0.0 .. 1.0.
		 */
		float r = 0;
		float g = 0;
		float b = 0;
		/**
		 * intensity 0.0 .. 1.0 based on drop off in vision at low/high
		 * wavelengths
		 */
		float s = 1;
		/**
		 * We use different linear interpolations on different bands. These
		 * numbers mark the upper bound of each band. Wavelengths of the various
		 * bandbs.
		 */
		final float[] bands = { 380, 420, 440, 490, 510, 580, 645, 700, 780,
				Float.MAX_VALUE };
		/**
		 * Figure out which band we fall in. A point on the edge is considered
		 * part of the lower band.
		 */
		int band = bands.length - 1;
		for (int i = 0; i < bands.length; i++) {
			if (wl <= bands[i]) {
				band = i;
				break;
			}
		}
		switch (band) {
		case 0:
			/* invisible below 380 */
			// The code is a little redundant for clarity.
			// A smart optimiser can remove any r=0, g=0, b=0.
			r = 0;
			g = 0;
			b = 0;
			s = 0;
			break;
		case 1:
			/* 380 .. 420, intensity drop off. */
			r = (440 - wl) / (440 - 380);
			g = 0;
			b = 1;
			s = .3f + .7f * (wl - 380) / (420 - 380);
			break;
		case 2:
			/* 420 .. 440 */
			r = (440 - wl) / (440 - 380);
			g = 0;
			b = 1;
			break;
		case 3:
			/* 440 .. 490 */
			r = 0;
			g = (wl - 440) / (490 - 440);
			b = 1;
			break;
		case 4:
			/* 490 .. 510 */
			r = 0;
			g = 1;
			b = (510 - wl) / (510 - 490);
			break;
		case 5:
			/* 510 .. 580 */
			r = (wl - 510) / (580 - 510);
			g = 1;
			b = 0;
			break;
		case 6:
			/* 580 .. 645 */
			r = 1;
			g = (645 - wl) / (645 - 580);
			b = 0;
			break;
		case 7:
			/* 645 .. 700 */
			r = 1;
			g = 0;
			b = 0;
			break;
		case 8:
			/* 700 .. 780, intensity drop off */
			r = 1;
			g = 0;
			b = 0;
			s = .3f + .7f * (780 - wl) / (780 - 700);
			break;
		case 9:
			/* invisible above 780 */
			r = 0;
			g = 0;
			b = 0;
			s = 0;
			break;
		} // end switch
			// apply intensity and gamma corrections.
		s *= gamma;
		r *= s;
		g *= s;
		b *= s;
		return new Color(r, g, b);
	} // end wvColor
}
