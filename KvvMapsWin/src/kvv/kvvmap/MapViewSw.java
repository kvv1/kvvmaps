package kvv.kvvmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComponent;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.maps.Maps;
import kvv.kvvmap.common.maps.MapsDir;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import kvv.kvvmap.common.view.CommonView;
import kvv.kvvmap.common.view.Environment;
import kvv.kvvmap.common.view.IPlatformView;

public class MapViewSw extends JComponent implements IPlatformView {
	private static final long serialVersionUID = 1L;

	private static final int SCR_W = 320 * 2;
	private static final int SCR_H = 384 * 2;
//	private static final int SCR_W = 320;
//	private static final int SCR_H = 384;

	@Override
	protected void paintComponent(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;
		GC gc = new GC(g, SCR_W, SCR_H);
		commonView.draw(gc);

		if (commonView.isMultiple()) {
			g.setColor(Color.CYAN);
			g.fillOval(100, 30, 30, 30);
		}

		g.setClip(null);
		g.setColor(Color.BLACK);
		List<String> maps = commonView.getCenterMaps();
		int y = 40;
		for (String map : maps) {
			g.drawString(map, SCR_W + 20, y);
			y += 40;
		}

		updateTitle();
	}

	private final CommonView commonView;
	private final SwingWnd swingWnd;

	private final Environment envir;
	
	public MapViewSw(SwingWnd swingWnd) {
		this.swingWnd = swingWnd;
		setSize(SCR_W, SCR_H);
		setPreferredSize(new Dimension(SCR_W, SCR_H));

		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);

		Adapter adapter = new Adapter();
		MapsDir mapsDir = new MapsDir();
		envir = new Environment(adapter, new Paths(), new PlaceMarks(), new Maps(adapter, mapsDir), mapsDir);
		
		commonView = new CommonView(this, envir);

		animateTo(new LocationX(30, 60));

		commonView.loadState();
	}

	private MouseAdapter mouseAdapter = new MouseAdapter() {

		@Override
		public void mouseDragged(MouseEvent e) {
			commonView.onMove(e.getX(), e.getY());
		}

		@Override
		public void mousePressed(MouseEvent e) {
			commonView.onDown(e.getX(), e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			commonView.onUp(e.getX(), e.getY());
		}

	};

	public void zoomOut() {
		commonView.zoomOut();
	}

	public void zoomIn() {
		commonView.zoomIn();
	}

	public void reorderMaps() {
		commonView.reorderMaps();
	}

	private void updateTitle() {
		String lon = Utils.format(commonView.getLocation().getLongitude());
		String lat = Utils.format(commonView.getLocation().getLatitude());
		String title = lon + " " + lat + " z" + commonView.getZoom() + " "
				+ getTopMap();
		swingWnd.setTitle(title);
	}

	private String getTopMap() {
		return commonView.getTopMap();
	}

	public void animateTo(LocationX loc) {
		commonView.animateTo(loc);
	}

	public void saveState() {
		commonView.saveState();
	}

	public void incInfoLevel() {
		commonView.incInfoLevel();
	}

	public void decInfoLevel() {
		commonView.decInfoLevel();
	}

	public void setTarget() {
		ISelectable sel = commonView.getSel();
		if (sel instanceof LocationX) {
			envir.placemarks.setTarget((LocationX) sel);
		}
	}

}
