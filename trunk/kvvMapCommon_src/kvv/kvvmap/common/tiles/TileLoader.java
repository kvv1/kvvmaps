package kvv.kvvmap.common.tiles;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.Pair;

public abstract class TileLoader {
	private final Adapter adapter;
	
	public TileLoader (Adapter adapter) {
		this.adapter = adapter;
	}
	
	//private static void log(String s) {
	//}

	public interface TileLoaderCallback {
		void loaded(Tile tile);
	}

	protected abstract Tile loadAsync(long id);

	private final LinkedList<Pair<Long, TileLoaderCallback>> queue = new LinkedList<Pair<Long, TileLoaderCallback>>();
	private final LinkedList<Pair<Long, TileLoaderCallback>> queue1 = new LinkedList<Pair<Long, TileLoaderCallback>>();

	static List<Long> ids = new ArrayList<Long>();

	public static synchronized int getId(Long l) {
		int idx = ids.indexOf(l);
		if (idx >= 0)
			return idx;
		ids.add(l);
		return ids.size() - 1;
	}

	@SuppressWarnings("unused")
	private Thread thread = new Thread() {
		{
			setDaemon(true);
			setPriority((MIN_PRIORITY + NORM_PRIORITY) / 2);
			start();
		}

		public void run() {
			for (;;) {
				final Pair<Long, TileLoaderCallback> request;
				synchronized (queue) {
					while (queue.isEmpty())
						try {
							queue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					request = queue.removeFirst();
					queue1.add(request);
				}
				//log("loading " + TileLoader.getId(request.first));
				Tile tile = loadAsync(request.first);
				if (tile != null) {
					final Tile tile1 = tile;
					final TileLoaderCallback callback = request.second;
					adapter.exec(new Runnable() {
						@Override
						public void run() {
							synchronized (queue) {
								boolean found = false;
								for (Pair<Long, TileLoaderCallback> p : queue1) {
									if (p == request) {
										found = true;
										break;
									}
								}

								if (found) {
									queue1.remove(request);
									//log("notifying "
									//		+ TileLoader.getId(request.first));
									callback.loaded(tile1);
								} else {
									//log("cancelled "
									//		+ TileLoader.getId(request.first));
								}
							}
						}
					});
				}
			}
		}

	};

	public void load(Long id, final TileLoaderCallback callback, PointInt centerXY) {
		adapter.assertUIThread();
		//log("load " + getId(id));
		synchronized (queue) {
			for (Pair<Long, TileLoaderCallback> p : queue) {
				if (p.first.equals(id))
					return;
			}
			for (Pair<Long, TileLoaderCallback> p : queue1) {
				if (p.first.equals(id))
					return;
			}

			int d1 = Math.abs(TileId.nx(id) * Adapter.TILE_SIZE
					+ Adapter.TILE_SIZE / 2 - centerXY.x)
					+ Math.abs(TileId.ny(id) * Adapter.TILE_SIZE
							+ Adapter.TILE_SIZE / 2 - centerXY.y);

			ListIterator<Pair<Long, TileLoaderCallback>> it = queue.listIterator();
			while (it.hasNext()) {
				Pair<Long, TileLoaderCallback> p = it.next();
				long id1 = p.first;
				int d2 = Math.abs(TileId.nx(id1) * Adapter.TILE_SIZE
						+ Adapter.TILE_SIZE / 2 - centerXY.x)
						+ Math.abs(TileId.ny(id1) * Adapter.TILE_SIZE
								+ Adapter.TILE_SIZE / 2 - centerXY.y);
				if (d2 > d1) {
					it.previous();
					break;
				}
			}

			//log("adding " + getId(id));
			it.add(new Pair<Long, TileLoaderCallback>(id, callback));

			queue.notify();
		}
	}

	public void cancelLoading() {
		synchronized (queue) {
			//log("cancelling");
			queue.clear();
			queue1.clear();
		}
	}
}
