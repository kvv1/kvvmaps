package kvv.kvvmap.common.tiles;

import java.util.LinkedList;
import java.util.ListIterator;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.Pair;

public abstract class TileLoader {
	private final Adapter adapter;

	public TileLoader(Adapter adapter) {
		this.adapter = adapter;
		Adapter.log("TileLoader " + ++cnt);
	}

	// private static void log(String s) {
	// }

	private static int cnt; 
	
	protected abstract Tile loadAsync(long id);

	private final LinkedList<Pair<Long, TileLoaderCallback>> queue = new LinkedList<Pair<Long, TileLoaderCallback>>();
	private final LinkedList<Pair<Long, TileLoaderCallback>> queue1 = new LinkedList<Pair<Long, TileLoaderCallback>>();

	class LoaderThread extends Thread {
		{
			setDaemon(true);
			setPriority((MIN_PRIORITY + NORM_PRIORITY) / 2);
		}

		public void run() {
			for (;;) {
				final Pair<Long, TileLoaderCallback> request;
				synchronized (queue) {
					if (queue.isEmpty()) {
						thread = null;
						System.gc();
						return;
					}
					request = queue.removeFirst();
					queue1.add(request);
				}
				// log("loading " + TileLoader.getId(request.first));
				Tile tile = loadAsync(request.first);
				if (tile != null) {
					final Tile tile1 = tile;
					final TileLoaderCallback callback = request.second;
					adapter.exec(new Runnable() {
						@Override
						public void run() {
							synchronized (queue) {
								for (Pair<Long, TileLoaderCallback> p : queue1) {
									if (p == request) {
										queue1.remove(request);
										callback.loaded(tile1);
										break;
									}
								}
							}
						}
					});
				}
			}
		}
	}
	
	
/*
	ExecutorService msgQueue = Executors.newSingleThreadExecutor();
	
	Runnable r = new Runnable() {
		@Override
		public void run() {
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
		}
	};
*/	
	
	private Thread thread;

	public void load(Long id, final TileLoaderCallback callback,
			PointInt centerXY) {
		adapter.assertUIThread();
		// log("load " + getId(id));
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

			ListIterator<Pair<Long, TileLoaderCallback>> it = queue
					.listIterator();
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

			// log("adding " + getId(id));
			it.add(new Pair<Long, TileLoaderCallback>(id, callback));

			if (thread == null) {
				thread = new LoaderThread();
				thread.start();
			}
		}
	}

	public void cancelLoading() {
		synchronized (queue) {
			// log("cancelling");
			queue.clear();
			queue1.clear();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~TileLoader " + --cnt);
		super.finalize();
	}
}
