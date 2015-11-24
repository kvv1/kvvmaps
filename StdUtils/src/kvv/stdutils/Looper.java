package kvv.stdutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Looper {

	static class Task implements Comparable<Task> {
		final Runnable runnable;
		final long time;

		public Task(Runnable runnable, long time) {
			this.runnable = runnable;
			this.time = time;
		}

		@Override
		public int compareTo(Task o) {
			return Long.compare(time, o.time);
		}
	}

	private final ArrayList<Task> tasks = new ArrayList<>();

	public Looper() {
	}

	public synchronized void post(Runnable r) {
		post(r, 0);
	}

	public synchronized void post(Runnable r, long delay) {
		tasks.add(new Task(r, System.currentTimeMillis() + delay));
		Collections.sort(tasks);
		notifyAll();
	}

	public synchronized void remove(Runnable r) {
		Iterator<Task> it = tasks.listIterator();
		while (it.hasNext()) {
			Task t = it.next();
			if (t.runnable == r)
				it.remove();
		}
	}

	public synchronized void start() {
		stop();
		thread = new Thread1();
		thread.start();
	}

	public synchronized void stop() {
		if (thread != null)
			thread.close();
		thread = null;
	}

	private Thread1 thread;

	class Thread1 extends Thread {
		private volatile boolean stopped;

		public Thread1() {
			super("LOOPER");
			setPriority(MIN_PRIORITY);
		}

		void close() {
			stopped = true;
		}

		@Override
		public void run() {
			while (!stopped) {
				try {
					synchronized (Looper.this) {
						if (stopped)
							return;
						if (tasks.size() > 0) {
							Task task1 = tasks.get(0);
							long dt = task1.time - System.currentTimeMillis();
							if (dt <= 0) {
								tasks.remove(0);
								task1.runnable.run();
							} else {
								Looper.this.wait(Math.min(dt, 1000));
							}
						} else {
							Looper.this.wait(1000);
						}
					}
					yield();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("-stopped");
		}
	};

}
