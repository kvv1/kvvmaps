package kvv.kvvmap.common.pacemark;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kvv.kvvmap.adapter.LocationX;

public class Saver extends Thread {

	private Set<IPlaceMarks> toSave = new HashSet<IPlaceMarks>();

	{
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
	}

	private static Saver saver;

	public static void save(IPlaceMarks path) {
		synchronized (Saver.class) {
			if (saver == null) {
				saver = new Saver();
				saver.start();
			}
			saver.toSave.add(path);
		}
	}

	@Override
	public void run() {
		for (;;) {
			Set<IPlaceMarks> set;
			synchronized (Saver.class) {
				if (toSave.isEmpty()) {
					saver = null;
					return;
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}

			synchronized (Saver.class) {
				set = toSave;
				toSave = new HashSet<IPlaceMarks>();
			}

			try {
				for (IPlaceMarks path : set) {
					path.save();
				}
			} catch (IOException e) {
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void writePlacemarks(List<LocationX> pms, PrintStream s) {
		for (LocationX pm : pms) {
			if (pm.name != null)
				s.print("name=" + pm.name.replace(' ', '_') + " ");
			s.println("lon=" + pm.getLongitude() + " lat=" + pm.getLatitude()
					+ " alt=" + (int) pm.getAltitude() + " speed="
					+ pm.getSpeed() + " time=" + pm.getTime());
		}
	}

}
