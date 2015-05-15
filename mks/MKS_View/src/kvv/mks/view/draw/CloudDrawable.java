package kvv.mks.view.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import kvv.mks.cloud.Cloud;
import kvv.mks.cloud.Pt;
import kvv.mks.rot.M;
import kvv.mks.rot.Rot;

public class CloudDrawable extends DrawableCached {

	private Cloud pcd;
	private Color color;

	private double maxSz;

	private float[][] zbuf;

	private Rot maxtrix = M.rot(0, 0, 0);


	public CloudDrawable(Cloud pcd, Color color) {
		this.pcd = pcd;
		this.color = color;

		for (Pt pt : pcd.data) {
			double r = pt.mod();
			maxSz = Math.max(maxSz, 2 * r);
		}

		cacheOn();
	}

	int d = 3;

	@Override
	public void _draw(Graphics2D g, int w, int h) {

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);

		zbuf = new float[w][h];

		if (zbuf == null || zbuf.length != w || zbuf[0].length != h) {
			zbuf = new float[w + 10][h + 10];
		}

		for (int i = 0; i < zbuf.length; i++)
			Arrays.fill(zbuf[i], -1000);

		// TODO Auto-generated method stub
		double mul = Math.min(w, h) / maxSz;

		Color oldColor = g.getColor();
		g.setColor(color);

		Pt pt1 = new Pt();

		for (Pt pt : pcd.data) {
			maxtrix.apply(pt, pt1);
			int _x = (int) (pt1.x * mul + w / 2);
			int _y = (int) (pt1.y * mul + h / 2);

			int c = 255 - (int) (255 * (pt1.z + maxSz / 2) / maxSz);
			if (c < 0)
				c = 0;
			if (c > 255)
				c = 255;
			Color color = new Color(c, c, c);
			g.setColor(color);

			for (int i = 0; i < d; i++)
				for (int j = 0; j < d; j++) {
					int x = _x + i;
					int y = _y + j;

					try {
						if (pt1.z > zbuf[x][y]) {
							g.fillRect(x, y, 1, 1);
							zbuf[x][y] = (float) pt1.z;
						}
					} catch (Exception e) {
					}

				}

		}
		g.setColor(oldColor);
	}

	public void setMatrix(Rot matrix) {
		this.maxtrix = matrix;
		invalidateCache();
	}
}
