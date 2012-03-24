package kvv.kvvmap.common.tiles;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import kvv.kvvmap.adapter.Adapter;

public class TileLoader {
	private final Adapter adapter;
	private final TileSource tileSource;

	public TileLoader(Adapter adapter, TileSource tileSource) {
		this.adapter = adapter;
		this.tileSource = tileSource;
		Adapter.log("TileLoader " + ++cnt);
	}

	// private static void log(String s) {
	// }

	private static int cnt;

	public interface TileSource {
		Tile loadAsync(long id);
	}

	static class Request {
		public Request(long id, TileLoaderCallback callback, int requestId) {
			this.id = id;
			this.callback = callback;
			this.requestId = requestId;
		}

		long id;
		TileLoaderCallback callback;
		int requestId;
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
			Tile tile = tileSource.loadAsync(request.id);
			if (tile != null) {
				final Tile tile1 = tile;
				final TileLoaderCallback callback = request.callback;
				adapter.execUI(new Runnable() {
					@Override
					public void run() {
						synchronized (queue) {
							if (processingRequests.remove(request))
								callback.loaded(tile1);
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

	public void load(Long id, final TileLoaderCallback callback, int centerX,
			int centerY) {
		adapter.assertUIThread();
		// log("load " + getId(id));
		synchronized (queue) {
			for (Request p : queue)
				if (p.id == id)
					return;

			for (Request p : processingRequests)
				if (p.id == id)
					return;

			int d1 = Math.abs(TileId.nx(id) * Adapter.TILE_SIZE
					+ Adapter.TILE_SIZE / 2 - centerX)
					+ Math.abs(TileId.ny(id) * Adapter.TILE_SIZE
							+ Adapter.TILE_SIZE / 2 - centerY);

			ListIterator<Request> it = queue.listIterator();
			while (it.hasNext()) {
				Request p = it.next();
				long id1 = p.id;
				int d2 = Math.abs(TileId.nx(id1) * Adapter.TILE_SIZE
						+ Adapter.TILE_SIZE / 2 - centerX)
						+ Math.abs(TileId.ny(id1) * Adapter.TILE_SIZE
								+ Adapter.TILE_SIZE / 2 - centerY);
				if (d2 > d1) {
					it.previous();
					break;
				}
			}

			// log("adding " + getId(id));
			it.add(new Request(id, callback, 0));

			adapter.execBG(r);

		}
	}

	public void cancelLoading() {
		synchronized (queue) {
			// log("cancelling");
			queue.clear();
			processingRequests.clear();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~TileLoader " + --cnt);
		super.finalize();
	}
}
