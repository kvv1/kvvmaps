package kvv.kvvmap.common.pacemark;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;

public class Saver extends Thread {

	private Set<IPlaceMarks> toSave = new HashSet<IPlaceMarks>();
	public volatile boolean stopped;

	{
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

	private volatile static Saver saver;

	public synchronized static Saver getInstance() {
		if (saver == null)
			saver = new Saver();
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
						if (stopped) {
							Adapter.log("Saver stopped");
							saver = null;
							return;
						}
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

	public static synchronized void dispose() {
		if (saver != null) {
			saver.stopped = true;
			saver.interrupt();
		}
	}

}
