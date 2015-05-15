package kvv.mks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import kvv.mks.cloud.Pt;

public class Util {
	public static double g2r(double g) {
		return g * Math.PI / 180;
	}

	public static double r2g(double r) {
		return r * 180 / Math.PI;
	}

	public static double rand(double min, double max) {
		return min + Math.random() * (max - min);
	}

	public static void drawText(Graphics2D g, String txt, int x, int y) {
		int w = g.getFontMetrics().stringWidth(txt);
		int h = g.getFontMetrics().getHeight();
		int descent = g.getFontMetrics().getDescent();
		int leading = g.getFontMetrics().getLeading();

		Color oldColor = g.getColor();
		g.setColor(Color.WHITE);
		g.fillRect(x, y - (-leading - descent) / 2, w, (h - leading - descent));
		g.setColor(Color.BLACK);
		g.drawString(txt, x, y + h - descent);
		g.setColor(oldColor);
	}

	public static double dist2(Double... dist) {
		double sum = 0;
		for (double d : dist)
			sum += d * d;
		return Math.sqrt(sum);
	}

	public static double aver2(Double... dist) {
		double sum = 0;
		for (double d : dist)
			sum += d * d;
		return Math.sqrt(sum / dist.length);
	}

	public static String getScale(int len, double max, double value) {
		StringBuilder sb = new StringBuilder();
		if (value > max) {
			for (int i = 0; i < len; i++)
				sb.append('#');
		} else {
			for (int i = 0; i < len; i++)
				if (max * i / len < value)
					sb.append('*');
				else
					sb.append('.');
		}
		return sb.toString();
	}

}
