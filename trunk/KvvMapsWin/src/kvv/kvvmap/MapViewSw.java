package kvv.kvvmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.maps.Maps;
import kvv.kvvmap.common.maps.MapsDir;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import kvv.kvvmap.common.view.CommonView;
import kvv.kvvmap.common.view.Environment;
import kvv.kvvmap.common.view.IPlatformView;

public class MapViewSw extends JComponent {
	private static final long serialVersionUID = 1L;

	private static final int SCR_W = 320 * 2;
	private static final int SCR_H = 384 * 2;

	// private static final int SCR_W = 320;
	// private static final int SCR_H = 384;

	@Override
	protected void paintComponent(Graphics _g) {
		
		Runtime.getRuntime().freeMemory();
		Runtime.getRuntime().maxMemory();
		Runtime.getRuntime().totalMemory();
		
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

	public MapViewSw(SwingWnd swingWnd) throws IOException {
		this.swingWnd = swingWnd;
		setSize(SCR_W, SCR_H);
		setPreferredSize(new Dimension(SCR_W, SCR_H));

		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);

		File[] files = new File(Adapter.MAPS_ROOT).listFiles();

		Adapter adapter = new Adapter();
		MapsDir mapsDir = new MapsDir(files);
		envir = new Environment(adapter, new Paths(), new PlaceMarks(),
				new Maps(adapter, mapsDir));

		commonView = new CommonView(new IPlatformView() {

			@Override
			public void repaint() {
				MapViewSw.this.repaint();
			}

			@Override
			public int getWidth() {
				return SCR_W;
			}

			@Override
			public int getHeight() {
				return SCR_H;
			}

			@Override
			public void getLocationOnScreen(int[] res) {
			}

			@Override
			public boolean loadDuringScrolling() {
				return true;
			}

			@Override
			public void pathSelected(PathSelection sel) {
				// TODO Auto-generated method stub
				
			}
		}, envir);

		animateTo(new LocationX(30, 60));

		loadState();
	}

	private MouseAdapter mouseAdapter = new MouseAdapter() {

		private Point p;

		@Override
		public void mouseDragged(MouseEvent e) {
			if (p != null) {
				commonView.scrollBy(p.x - e.getX(), p.y - e.getY());
				p = new Point(e.getX(), e.getY());
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			p = new Point(e.getX(), e.getY());
			commonView.startScrolling();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			p = null;
			commonView.endScrolling();
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
		String lon = Utils.formatLatLon(commonView.getLocation().getLongitude());
		String lat = Utils.formatLatLon(commonView.getLocation().getLatitude());
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

	@SuppressWarnings("deprecation")
	public void saveState() {
		Properties props = new Properties();

		String sLon = Double.toString(commonView.getLocation().getLongitude());
		String sLat = Double.toString(commonView.getLocation().getLatitude());
		String sZoom = Integer.toString(commonView.getZoom());

		props.put("lon", sLon);
		props.put("lat", sLat);
		props.put("zoom", sZoom);
		try {
			props.save(new FileOutputStream("a.properties"), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void loadState() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("a.properties"));
		} catch (IOException e1) {
		}

		String sLon = props.getProperty("lon");
		String sLat = props.getProperty("lat");
		String sZoom = props.getProperty("zoom");

		if (sLon != null && sLat != null && sZoom != null) {
			double lon = Double.parseDouble(sLon);
			double lat = Double.parseDouble(sLat);
			int zoom = Integer.parseInt(sZoom);
			commonView.setZoom(zoom);
			commonView.animateTo(new LocationX(lon, lat));
		}

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

	public void setLarge(boolean selected) {
		Adapter.TILE_SIZE = selected ? 512 : 256;
		commonView.invalidateTiles();
	}

}
