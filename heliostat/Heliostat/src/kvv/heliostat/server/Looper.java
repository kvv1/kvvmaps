package kvv.heliostat.server;

import java.util.ArrayList;
import java.util.Collections;

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
			return time < o.time ? -1 : time > o.time ? 1 : 0;
		}
	}

	private final ArrayList<Task> tasks = new ArrayList<>();

	public Looper() {
	}

	public void post(Runnable r) {
		post(r, 0);
	}

	public void post(Runnable r, long delay) {
		synchronized (tasks) {
			tasks.add(new Task(r, System.currentTimeMillis() + delay));
			Collections.sort(tasks);
			tasks.notifyAll();
		}
	}

	public void stop() {
		if (thread != null)
			thread.close();
		thread = null;
	}

	private Thread1 thread;

	class Thread1 extends Thread {
		private volatile boolean stopped;

		void close() {
			stopped = true;
			interrupt();
		}

		@Override
		public void run() {
			while (!stopped) {
				try {
					Task task = null;
					synchronized (tasks) {
						if (stopped)
							return;
						if (tasks.size() > 0) {
							Task task1 = tasks.get(0);
							long dt = task1.time - System.currentTimeMillis();
							if (dt <= 0) {
								tasks.remove(0);
								task = task1;
							} else {
								tasks.wait(dt);
							}
						} else {
							tasks.wait();
						}
					}

					if (stopped)
						return;
					
					if (task != null)
						task.runnable.run();
				} catch (Exception e) {
				}
			}
		}
	};

	public void start() {
		stop();
		thread = new Thread1();
		thread.start();
	}
}
