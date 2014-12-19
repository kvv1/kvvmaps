package kvv.mks.draw;

import java.awt.Color;
import java.awt.Graphics2D;

import kvv.mks.cloud.Cloud;
import kvv.mks.cloud.Pt;
import kvv.mks.rot.M;
import kvv.mks.rot.Rot;
import kvv.mks.rot.matrix.Matrix3x3;

public class CloudDrawable extends DrawableCached{

	private Cloud pcd;
	private Color color;

	private double maxSz;
	
	
	public CloudDrawable(Cloud pcd, Color color) {
		this.pcd = pcd;
		this.color = color;
		maxSz = pcd.getMaxSize();
		cacheOn();
	}
	
	int d = 3;
	
	private Rot maxtrix = M.instance.create();
	
	@Override
	public void _draw(Graphics2D g, int w, int h) {
		// TODO Auto-generated method stub
		double mul = Math.min(w, h) / maxSz;

		Color oldColor = g.getColor();
		g.setColor(color);
		
		Pt pt1 = new Pt();
		
		for (Pt pt : pcd.data) {
			maxtrix.apply(pt, pt1);
			int x = (int) (pt1.x * mul + w / 2);
			int y = (int) (pt1.y * mul + h / 2);
			g.fillRect(x, y, d, d);
		}
		
		g.setColor(oldColor);
	}

	public void setMatrix(Rot matrix) {
		this.maxtrix = matrix;
		invalidateCache();
	}
}
