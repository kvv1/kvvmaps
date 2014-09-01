package kvv.kvvmap.tiles;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

public abstract class AbstractLoader<K, V> {
	protected abstract void loaded(V tile);
	protected abstract V loadAsync(K id);
	protected abstract void execUI(Runnable r);
	protected abstract void execBG(Runnable r);
	protected abstract int getPrio(K k);
	
	private class Request {
		public Request(K id) {
			this.id = id;
		}
		K id;
	}

	private final LinkedList<Request> queue = new LinkedList<Request>();
	private final Set<Request> processingRequests = new HashSet<Request>();

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			final Request request;
			synchronized (queue) {
				if (queue.isEmpty())
					return;
				request = queue.removeFirst();
				processingRequests.add(request);
			}
			final V tile = loadAsync(request.id);
			if (tile != null) {
				execUI(new Runnable() {
					@Override
					public void run() {
						synchronized (queue) {
							if (processingRequests.remove(request))
								loaded(tile);
						}
					}
				});
			} else {
				synchronized (queue) {
					processingRequests.remove(request);
				}
			}
		}
	};

	public void load(K id) {
		synchronized (queue) {
			for (Request p : queue)
				if (p.id.equals(id))
					return;

			for (Request p : processingRequests)
				if (p.id.equals(id))
					return;

			int d1 = getPrio(id);
			
			ListIterator<Request> it = queue.listIterator();
			while (it.hasNext()) {
				Request p = it.next();
				int d2 = getPrio(p.id);
				if (d2 > d1) {
					it.previous();
					break;
				}
			}

			// log("adding " + getId(id));
			it.add(new Request(id));

			execBG(r);

		}
	}

	public void cancelLoading() {
		synchronized (queue) {
			// log("cancelling");
			queue.clear();
			processingRequests.clear();
		}
	}
}
