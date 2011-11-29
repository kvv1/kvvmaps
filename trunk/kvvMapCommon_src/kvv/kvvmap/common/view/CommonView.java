package kvv.kvvmap.common.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.PathSelection;

public class CommonView implements ICommonView {

	private final IPlatformView platformViewView;

	private final CommonDoc doc;

	private LocationX myLocation;
	private boolean myLocationDimmed;
	private final Environment envir;

	public CommonView(IPlatformView platformViewView, Environment envir) {
		this.envir = envir;
		this.platformViewView = platformViewView;
		this.doc = new CommonDoc(this, envir);
		// myLocation = new LocationX(30, 60, 0, 1000, 0, 0);
	}

	public LocationX getMyLocation() {
		return myLocation;
	}

	public boolean isMyLocationDimmed() {
		return myLocationDimmed;
	}

	public void setMyLocation(LocationX locationX, boolean forceScroll) {
		forceScroll |= (isOnMyLocation() && !isHere(locationX));

		myLocation = locationX;
		myLocationDimmed = false;

		if (forceScroll && locationX != null)
			animateTo(locationX);
		else
			repaint();
	}

	public void dimmMyLocation() {
		myLocationDimmed = true;
	}

	private boolean isHere(LocationX loc) {
		if (loc == null)
			return false;

		int x1 = loc.getX(getZoom());
		int y1 = loc.getY(getZoom());

		PointInt center = doc.getCenterXY();
		int x2 = center.x;
		int y2 = center.y;

		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) < 20;
	}

	public boolean isOnMyLocation() {
		return isHere(myLocation);
	}

	private PointInt p1;

	private KineticScrollingFilter ksf = new KineticScrollingFilterImpl1() {

		@Override
		public void onMousePressed(int x, int y) {
			p1 = new PointInt(x, y);
		}

		@Override
		public void onMouseReleased(int x, int y) {
			p1 = null;
			doc.updateSel();
			update();
		}

		@Override
		public void onMouseDragged(int x, int y) {
			if (p1 != null) {
				PointInt offset = new PointInt(p1.x - x, p1.y - y);
				doc.animateBy(offset);
				p1 = new PointInt(x, y);
			}
		}

		@Override
		public void exec(Runnable r) {
			envir.adapter.exec(r);
		}

	};

	public void onMove(int x, int y) {
		ksf.mouseDragged(x, y);
	}

	public void onDown(int x, int y) {
		ksf.mousePressed(x, y);
	}

	public void onUp(int x, int y) {
		ksf.mouseReleased(x, y);
	}

	public void zoomOut() {
		doc.zoomOut();
		doc.updateSel();
		update();
	}

	public void zoomIn() {
		doc.zoomIn();
		doc.updateSel();
		update();
	}

	public boolean canReorder() {
		return doc.isMultiple();
	}

	public List<String> getCenterMaps() {
		return doc.getCenterMaps();
	}

	public void reorderMaps() {
		doc.reorderMaps();
		update();
	}

	public int getZoom() {
		return doc.getZoom();
	}

	public void setZoom(int zoom) {
		doc.setZoom(zoom);
		doc.updateSel();
		update();
	}

	public LocationX getLocation() {
		return doc.getLocation();
	}

	public void animateTo(LocationX loc) {
		doc.animateTo(loc);
		doc.updateSel();
		update();
	}

	public void animateTo(LocationX loc, int dx, int dy) {
		doc.animateTo(loc, dx, dy);
		doc.updateSel();
		update();
	}

	public void draw(GC gc) {
		gc.setAntiAlias(true);
		// long time = System.currentTimeMillis();
		doc.draw(gc);
		// long time1 = System.currentTimeMillis();
		// System.out.println("t1 = " + (time1 - time));
		int locationH = drawMyLocation(gc);
		drawCross(gc);
		drawScale(gc);
		// long time2 = System.currentTimeMillis();
		// System.out.println("t2 = " + (time2 - time1));
		ISelectable sel = getSel();
		if (p1 == null) {
			if (doc.getInfoLevel().ordinal() != 0
					&& (sel instanceof PathSelection)) {
				PathSelection sel1 = (PathSelection) sel;
				sel1.path.drawDiagram(gc, gc.getHeight() - locationH, sel1.pm);
			}
			drawTarget(gc);
		}
	}

	private void drawTarget(GC gc) {
		LocationX targ = doc.getTarget();
		if (targ == null)
			return;

		PointInt center = doc.getCenterXY();
		int _dx = Math.abs(center.x - targ.getX(getZoom()));
		int _dy = Math.abs(center.y - targ.getY(getZoom()));
		if (_dx > gc.getWidth() / 3 || _dy > gc.getHeight() / 3) {
			gc.setColor(COLOR.TARG_COLOR);
			gc.setStrokeWidth(2);

			int len = gc.getWidth() / 16;

			double bearing = (90 - getLocation().bearingTo(targ)) * Math.PI
					/ 180;

			int dx = (int) (len * Math.cos(bearing));
			int dy = (int) (len * Math.sin(bearing));

			int x = gc.getWidth() / 2;
			int y = gc.getHeight() / 2;

			gc.drawLine(x, y, x + dx, y - dy);

		}

		LocationX myLoc = myLocation;
		if (myLoc != null && !isMyLocationDimmed()) {
			String txt = Utils.formatDistance((int) myLoc.distanceTo(targ));
			gc.setTextSize(gc.getHeight() / 24);

			RectX txtBounds = gc.getTextBounds(txt);

			PointInt pt = new PointInt((int) (gc.getWidth() - gc.getWidth()
					/ 10 - txtBounds.getWidth() / 2),
					(int) (gc.getHeight() / 5 + txtBounds.getHeight()));

			drawText(gc, txt, pt, COLOR.WHITE, COLOR.TARG_COLOR);
		}
	}

	private void drawCross(GC gc) {
		gc.setColor(0xFF000000);
		gc.setStrokeWidth(2);
		int x = gc.getWidth() / 2;
		int y = gc.getHeight() / 2;
		int sz = gc.getWidth() / 16;
		gc.drawLine(x, y - sz, x, y + sz);
		gc.drawLine(x - sz, y, x + sz, y);
	}

	private int getScale(int m) {
		int temp = m;
		int mul = 1;
		while (temp >= 10) {
			temp /= 10;
			mul *= 10;
		}

		if (temp >= 5)
			return 5 * mul;
		else if (temp >= 2)
			return 2 * mul;
		else
			return mul;
	}

	private void drawScale(GC gc) {
		int lineHeight = gc.getHeight() / 24;
		int scaleWidth = 5;

		int m = (int) doc.pt2m(gc.getWidth() / 2);
		int m1 = getScale(m);

		gc.setStrokeWidth(1);

		int len = gc.getWidth() / 2 * m1 / m;

		int x0 = 4;
		int x = x0;
		int y = lineHeight + scaleWidth + scaleWidth;

		gc.setColor(COLOR.BLACK);
		gc.fillRect(x, y, x + len / 4, y + scaleWidth);
		gc.fillRect(x + len / 2, y, x + len * 3 / 4, y + scaleWidth);
		gc.setColor(COLOR.WHITE);
		gc.fillRect(x + len / 4, y, x + len * 2 / 4, y + scaleWidth);
		gc.fillRect(x + len * 3 / 4, y, x + len, y + scaleWidth);
		gc.setColor(COLOR.BLACK);
		gc.drawRect(x, y, x + len, y + scaleWidth);
		gc.setColor(COLOR.WHITE);
		gc.drawRect(x + 1, y + 1, x + len - 1, y + scaleWidth - 1);
		gc.setColor(COLOR.BLACK);

		gc.setTextSize(lineHeight);
		String text = Float.toString((float) m1 / 1000) + "km";
		if ((m1 >= 1000) && (m1 % 1000 == 0))
			text = Integer.toString(m1 / 1000) + "km";
		else
			text = Float.toString((float) m1 / 1000) + "km";

		drawText(gc, text, new PointInt(4, lineHeight), COLOR.BLACK, 0x80FFFFFF);

		// RectX bounds = gc.getTextBounds(text);
		// bounds.offset(4, lineHeight);
		// bounds.inset(-1, -1);
		// gc.setColor(0x80FFFFFF);
		// gc.fillRect((float) (bounds.getX()), (float) (bounds.getY()),
		// (float) (bounds.getX() + bounds.getWidth()),
		// (float) (bounds.getY() + bounds.getHeight()));
		//
		// gc.setColor(COLOR.BLACK);
		// gc.drawText(text, 4, lineHeight);
	}

	private void drawText(GC gc, String text, PointInt pt, int textColor,
			int backgroundColor) {
		RectX bounds = gc.getTextBounds(text);
		bounds.offset(pt.x, pt.y);
		bounds.inset(-1, -1);
		gc.setColor(backgroundColor);
		gc.fillRect((float) (bounds.getX()), (float) (bounds.getY()),
				(float) (bounds.getX() + bounds.getWidth()),
				(float) (bounds.getY() + bounds.getHeight()));

		gc.setColor(textColor);
		gc.drawText(text, pt.x, pt.y);
	}

	private int drawMyLocation(GC gc) {
		LocationX loc = myLocation;
		if (loc == null)
			return 0;

		PointInt center = doc.getCenterXY();
		int ptx = loc.getX(getZoom());
		int pty = loc.getY(getZoom());
		int x = ptx - (center.x - gc.getWidth() / 2);
		int y = pty - (center.y - gc.getHeight() / 2);

		double accPt = doc.m2pt(loc.getAccuracy());

		if (accPt > 10) {
			gc.setColor(COLOR.MAGENTA & 0x80FFFFFF);
			gc.fillCircle(x, y, (float) accPt);
		}

		int lineHeight = gc.getHeight() / 24;
		gc.setTextSize(lineHeight);

		gc.setColor(0xA0000000);
		gc.fillRect(0, gc.getHeight() - lineHeight, gc.getWidth(),
				gc.getHeight());

		gc.setColor(COLOR.CYAN);
		gc.drawText(
				Utils.format(loc.getLongitude()) + " "
						+ Utils.format(loc.getLatitude()) + " "
						+ loc.getAltitude() + "m " + loc.getSpeed() * 3.6f
						+ "km/h", 0, gc.getHeight() - 2);

		// drawArrow(gc, x, y, getMyLocation().getBearing());
		gc.drawArrow(x, y, loc, myLocationDimmed);

		return lineHeight;
	}

	public void saveState() {
		Properties props = new Properties();

		String sLon = Double.toString(getLocation().getLongitude());
		String sLat = Double.toString(getLocation().getLatitude());
		String sZoom = Integer.toString(getZoom());

		props.put("lon", sLon);
		props.put("lat", sLat);
		props.put("zoom", sZoom);
		try {
			props.save(new FileOutputStream("a.properties"), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void loadState() {
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
			setZoom(zoom);
			animateTo(new LocationX(lon, lat));
		}

	}

	@Override
	public void update() {
		platformViewView.repaint();
	}

	@Override
	public void repaint() {
		platformViewView.repaint();
	}

	public ISelectable getSel() {
		return doc.getSelAsync();
	}

	public void dispose() {
		doc.dispose();
	}

	public void incInfoLevel() {
		doc.incInfoLevel();
	}

	public void decInfoLevel() {
		doc.decInfoLevel();
	}

	public void clearPathTiles() {
		doc.clearPathTiles();
	}

	public String getTopMap() {
		return doc.getTopMap();
	}

	public void setTarget() {
		doc.setTarget();
	}

	public LocationX getTarget() {
		return doc.getTarget();
	}

	public InfoLevel getInfoLevel() {
		return doc.getInfoLevel();
	}

	public LocationX getLocation(int dx, int dy) {
		return doc.getLocation(dx, dy);
	}

	public void setTopMap(String map) {
		doc.setTopMap(map);
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~CommonView");
		super.finalize();
	}
}
