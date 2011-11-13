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
		start();
	}

	private static final Saver saver = new Saver();

	public static Saver getInstance() {
		return saver;
	}

	public synchronized void save(IPlaceMarks path) {
		toSave.add(path);
		notify();
	}

	@Override
	public void run() {
		for (;;) {
			Set<IPlaceMarks> set;
			synchronized (this) {
				while (toSave.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			
			synchronized (this) {
				set = toSave;
				toSave = new HashSet<IPlaceMarks>();
			}
			
			try {
				for(IPlaceMarks path : set) {
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
