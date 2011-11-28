package kvv.kvvmap.common.pacemark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;

public class PlaceMarks implements IPlaceMarks {

	private final List<LocationX> placemarks = new CopyOnWriteArrayList<LocationX>();
	private volatile LocationX targ;
	private volatile IPlaceMarksListener doc;

	private static final File file = new File(Adapter.PLACEMARKS);

	public PlaceMarks() {
		BufferedReader rd = null;
		List<LocationX> pms;
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));
			pms = PathIO.readPlaceMarks(rd, null);
			set(pms);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rd != null)
					rd.close();
			} catch (IOException e) {
			}
		}
	}

	public void setDoc(IPlaceMarksListener l) {
		doc = l;
	}

	public void add(LocationX pm) {
		placemarks.add(pm);
		Saver.getInstance().save(this);
		IPlaceMarksListener doc = this.doc;
		if (doc != null)
			doc.onPathTilesChanged();
	}

	public List<LocationX> getPlaceMarks() {
		return placemarks;
	}

	public void remove(LocationX pm) {
		placemarks.remove(pm);
		Saver.getInstance().save(this);
		IPlaceMarksListener doc = this.doc;
		if (doc != null)
			doc.onPathTilesChanged();
	}

	public void set(List<LocationX> pms) {
		placemarks.clear();
		placemarks.addAll(pms);
		IPlaceMarksListener doc = this.doc;
		if (doc != null)
			doc.onPathTilesChanged();
	}

	public void setTarget(LocationX targ) {
		this.targ = targ;
		IPlaceMarksListener doc = this.doc;
		if (doc != null)
			doc.onPathTilesChanged();
	}

	public LocationX getTarget() {
		if (placemarks.contains(targ))
			return targ;
		return null;
	}

	@Override
	public void save() throws IOException {
		Adapter.log("saving placemarks");
		PrintStream ps = null;
		try {
			ps = new PrintStream(file, "UTF-8");
			Saver.writePlacemarks(getPlaceMarks(), ps);
			ps.flush();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public void replace(LocationX pm, LocationX pm1) {
		boolean target = targ == pm;
		if (pm != null)
			remove(pm);
		add(pm1);
		if (target)
			setTarget(pm1);
	}

	public void dispose() {
		placemarks.clear();
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Placemarks");
		super.finalize();
	}
}
