package kvv.mks.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import kvv.mks.cloud.Cloud;
import kvv.mks.cloud.Pt;
import kvv.mks.rot.Transform;

public class CloudDrawable extends DrawableCached {

	private Cloud pcd;

	private double maxSz;

	private float[][] zbuf;

	private Color[] colors = new Color[256];

	public CloudDrawable(Cloud pcd, Color color) {
		this.pcd = pcd;

		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		for (int i = 0; i < 256; i++) {
			int _r = (r * i + 240 * (256 - i)) / 256;
			int _g = (g * i + 240 * (256 - i)) / 256;
			int _b = (b * i + 240 * (256 - i)) / 256;
			colors[i] = new Color(_r, _g, _b);
		}

		for (Pt pt : pcd.data) {
			double mod = pt.mod();
			maxSz = Math.max(maxSz, 2 * mod);
		}

//		cacheOn();
	}

	private final static int d = 1;

	private Transform maxtrix = new Transform();

	double L = 0.5;

	@Override
	public void _draw(Graphics2D g, int w, int h) {

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);

		if (zbuf == null || zbuf.length != w || zbuf[0].length != h) {
			zbuf = new float[w][h];
		}

		for (int i = 0; i < zbuf.length; i++)
			Arrays.fill(zbuf[i], 1000);

		double mul = Math.min(w, h) / maxSz;

		Color oldColor = g.getColor();

		Pt pt1 = new Pt();

		for (Pt pt : pcd.data) {
			maxtrix.apply(pt, pt1);

			pt1.z += 100;

			if (pt1.z < L)
				continue;

			mul = 20;

			int _x = (int) (pt1.x * mul + w / 2);
			int _y = (int) (pt1.y * mul + h / 2);

			int cidx = Math.min(255,
					Math.max(0, (int) (255 * (pt1.z + maxSz / 2) / maxSz)));
			Color color = colors[255 - cidx];
			g.setColor(color);

			int x = _x / d;
			int y = _y / d;

			if (x < 0 || x >= zbuf.length || y < 0 || y >= zbuf[0].length)
				continue;
			if (pt1.z < zbuf[x][y]) {
				g.fillRect(x * d, y * d, d, d);
				zbuf[x][y] = (float) pt1.z;
			}
		}
		g.setColor(oldColor);
	}

	public void setMatrix(Transform matrix) {
		this.maxtrix = matrix;
		invalidateCache();
	}
}
