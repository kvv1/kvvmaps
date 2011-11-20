package kvv.kvvmap.common.pacemark;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.Utils;

public class Path implements IPlaceMarks {
	public final PathInZoom[] pathsInZooms = new PathInZoom[Utils.MAX_ZOOM + 1];
	private volatile File file;
	private volatile boolean enabled = true;
	private volatile IPlaceMarksListener doc;

	private volatile RectX rect;

	public Path(File file, List<LocationX> pms, boolean en,
			IPlaceMarksListener doc) {
		this.file = file;
		enabled = en;
		for (int zoom = Utils.MIN_ZOOM; zoom <= Utils.MAX_ZOOM; zoom++)
			pathsInZooms[zoom] = new PathInZoom(zoom);
		if (pms != null)
			set(pms);
		// after all!
		this.doc = doc;
		for (int zoom = Utils.MIN_ZOOM; zoom <= Utils.MAX_ZOOM; zoom++)
			pathsInZooms[zoom].setDoc(doc); // set doc after all intitialized
	}

	public List<LocationX> split(LocationX pm) {
		finishCompact();
		List<LocationX> placemarks = getPlaceMarks();
		int idx = placemarks.indexOf(pm);
		List<LocationX> sublist = placemarks.subList(0, idx);
		List<LocationX> sublist1 = placemarks.subList(idx, placemarks.size());
		set(sublist);
		Saver.getInstance().save(this);
		return sublist1;
	}

	public void set(List<LocationX> pms) {
		removeAll();
		for (LocationX pm : pms)
			addCompact(pm);
		finishCompact();
	}

	private void removeAll() {
		rect = null;
		for (int zoom = Utils.MIN_ZOOM; zoom <= Utils.MAX_ZOOM; zoom++)
			pathsInZooms[zoom].removeAll();
	}

	public boolean addCompact(LocationX pm) {
		boolean compacted = false;
		LocationX compactedPm = pm;
		for (int zoom = Utils.MAX_ZOOM; zoom >= Utils.MIN_ZOOM; zoom--) {
			compactedPm = pathsInZooms[zoom].addCompacted(compactedPm, pm);
			if (zoom == Utils.MAX_ZOOM && compactedPm != null)
				compacted = true;
		}
		if (rect == null)
			rect = new RectX(pm.getLongitude(), pm.getLatitude(), 0, 0);
		else
			rect.union(pm.getLongitude(), pm.getLatitude());

		if (compacted && doc != null)
			Saver.getInstance().save(this);

		return compacted;
	}

	public synchronized void finishCompact() {
		for (int zoom = Utils.MIN_ZOOM; zoom <= Utils.MAX_ZOOM; zoom++) {
			pathsInZooms[zoom].finishCompact();
		}

		if (doc != null)
			Saver.getInstance().save(this);
	}

	public void setFile(File newFile) {
		file = newFile;
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return file.getName();
	}

	public void setEnabled(boolean b) {
		enabled = b;
		if (doc != null) {
			doc.onPathTilesChanged();
			doc.updateSel();
			Saver.getInstance().save(this);
		}
	}

//	public void draw(GC gc, long id, InfoLevel infoLevel, ISelectable sel) {
//		if (!enabled)
//			return;
//		LocationX selPM = null;
//		if (sel instanceof PathSelection) {
//			PathSelection pathSel = (PathSelection) sel;
//			if (pathSel.path == this)
//				selPM = pathSel.pm;
//		}
//		pathsInZooms[TileId.zoom(id)].draw(gc, id, infoLevel, selPM);
//	}

	@Override
	public List<LocationX> getPlaceMarks() {
		return getPlaceMarks(Utils.MAX_ZOOM);
	}

	public List<LocationX> getPlaceMarks(int zoom) {
		return pathsInZooms[zoom].getPlaceMarks();
	}

	public float getLen() {
		List<LocationX> pms = getPlaceMarks();
		LocationX pm1 = null;
		float len = 0;
		for (LocationX pm : pms) {
			if (pm1 != null)
				len += pm.distanceTo(pm1);
			pm1 = pm;
		}
		return len;
	}

	public void drawDiagram(GC gc, int maxy, LocationX pm2) {
		int y = maxy;
		int w = gc.getWidth();

		int lineHeight = gc.getHeight() / 24;

		gc.setTextSize(lineHeight);

		RectX rect = new RectX(w * 2 / 6, y - 5 * lineHeight, w * 4 / 6,
				4 * lineHeight);
		gc.setColor(0x80000000);
		gc.fillRect(0, (float) (y - 5 * lineHeight), w, y);

		gc.setStrokeWidth(2);

		gc.setColor(COLOR.CYAN);
		gc.drawText(
				Utils.format(pm2.getLongitude()) + "  "
						+ Utils.format(pm2.getLatitude()) + "  "
						+ (int) pm2.getAltitude(), 2, y - 2);

		int len = Math.max(1, (int) getLen());

		gc.setColor(COLOR.GREEN);

		List<LocationX> pms = getPlaceMarks();

		if (pms.size() > 0) {
			int minAlt = (int) pms.iterator().next().getAltitude();
			int maxAlt = (int) pms.iterator().next().getAltitude();

			for (LocationX pm : pms) {
				minAlt = Math.min(minAlt, (int) pm.getAltitude());
				maxAlt = Math.max(maxAlt, (int) pm.getAltitude());
			}

			int altDif = Math.max(1, maxAlt - minAlt);

			LocationX prevPm = null;
			PointInt prevPt = null;

			float len0 = 0;

			PointInt pmPt = null;

			for (LocationX pm : pms) {
				if (prevPm != null)
					len0 += pm.distanceTo(prevPm);

				int _x = (int) (rect.getX() + (len0 * rect.getWidth() / len));
				int _y = (int) (rect.getY() + rect.getHeight() - (int) ((pm
						.getAltitude() - minAlt) * rect.getHeight() / altDif));

				PointInt pt = new PointInt(_x, _y);

				if (pm == pm2)
					pmPt = pt;

				if (prevPm != null)
					gc.drawLine(pt.x, pt.y, prevPt.x, prevPt.y);

				prevPm = pm;
				prevPt = pt;
			}

			gc.setColor(COLOR.CYAN);

			if (pmPt != null) {
				gc.fillCircle(pmPt.x, pmPt.y, 5);
			}

			gc.drawText("" + maxAlt + "m", 2,
					(int) (rect.getY() + lineHeight - 1));
			gc.drawText("" + minAlt + "m", 2, (int) (rect.getY() + lineHeight
					* 4 - 2));

			gc.drawText("" + len + "m", 2,
					(int) (rect.getY() + lineHeight * 2 - 2));
			gc.drawText("" + pms.size(), 2,
					(int) (rect.getY() + lineHeight * 3 - 2));
		}

		y -= 5 * lineHeight;

		if (file != null) {
			gc.setColor(0x80000000);
			gc.fillRect(0, y - lineHeight, w, y);
			gc.setColor(COLOR.CYAN);
			gc.drawText(file.getName(), 2, y - 2);
			y -= lineHeight;
		}
	}

	public boolean filter(RectX screenRect) {
		RectX rect = this.rect;
		if (rect == null)
			return false;
		return rect.intersects(screenRect);
	}

	@Override
	public void save() throws IOException {
		List<LocationX> pms = getPlaceMarks();
		if (pms.size() < 2)
			return;

		File f = File.createTempFile(file.getName(), "tmp",
				file.getParentFile());

		PrintStream ps = null;
		Adapter.log("saving path " + file);
		try {
			ps = new PrintStream(f, "UTF-8");
			ps.println("header enabled=" + enabled);
			Saver.writePlacemarks(pms, ps);
			ps.flush();
		} finally {
			if (ps != null)
				ps.close();
		}

		file.delete();
		f.renameTo(file);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public LocationX getNearest(LocationX l) {
		double dist = -1;
		LocationX pmNearest = null;

		for (LocationX pm : getPlaceMarks()) {
			double dx = pm.getLongitude() - l.getLongitude();
			double dy = pm.getLatitude() - l.getLatitude();
			double d = dx * dx + dy * dy;
			if (pmNearest == null || d < dist) {
				pmNearest = pm;
				dist = d;
			}
		}

		return pmNearest;
	}
}
