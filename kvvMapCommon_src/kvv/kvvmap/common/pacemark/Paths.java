package kvv.kvvmap.common.pacemark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;

public final class Paths {
	private final List<Path> paths = new CopyOnWriteArrayList<Path>();

	class PlaceMarksListener implements IPlaceMarksListener {

		private IPlaceMarksListener l;

		public synchronized void setListener(IPlaceMarksListener l) {
			this.l = l;
		}

		@Override
		public synchronized void onPathTileChanged(long id) {
			if (l != null)
				l.onPathTileChanged(id);
		}

		@Override
		public synchronized void onPathTilesChanged() {
			if (l != null)
				l.onPathTilesChanged();
		}

		@Override
		public void onPathTilesChangedAsync() {
			if (l != null)
				l.onPathTilesChangedAsync();
		}

	}

	private final PlaceMarksListener doc = new PlaceMarksListener();

	public Paths() {
		load();
	}

	public void setDoc(IPlaceMarksListener l) {
		doc.setListener(l);
	}

	public void clear() {
		paths.clear();
	}

	private void load() {
		if (paths.size() != 0)
			return;

		final File[] files = new File(Adapter.PATH_ROOT).listFiles();

		Thread thread = new Thread() {
			@Override
			public void run() {
				for (final File file : files) {
					try {
						long time = System.currentTimeMillis();
						boolean en = true;
						BufferedReader rd = new BufferedReader(
								new InputStreamReader(
										new FileInputStream(file), "UTF-8"));
						String line = rd.readLine();
						if (line != null) {
							if (line.startsWith("header")) {
								String[] tokens = PathIO.split(line, ' ');
								for (String token : tokens) {
									String[] namevals = PathIO
											.split(token, '=');
									if (namevals.length == 2) {
										String name = namevals[0];
										String value = namevals[1];
										if (name.equals("enabled"))
											en = Boolean.parseBoolean(value);
									}
								}
								line = null;
							}

							List<LocationX> pms = PathIO.readPlaceMarks(rd,
									line);
							final Path path = new Path(file, pms, en, doc);
							paths.add(path);
							time = System.currentTimeMillis() - time;
							Adapter.log("path " + path.getFile() + " added "
									+ time);
							//Adapter.logMem();
						}
						rd.close();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				doc.onPathTilesChangedAsync();
			}

		};

		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	public Path createPath(String name) {
		return createPath(null, getNewPathName());
	}

	private Path createPath(List<LocationX> pms, String name) {
		String pathName = name;
		File file = new File(Adapter.PATH_ROOT + "/" + pathName);
		Path path = new Path(file, pms, true, doc);
		paths.add(path);
		doc.onPathTilesChanged();
		return path;
	}

	public boolean rename(Path path, String newName) {
		File newFile = new File(Adapter.PATH_ROOT + "/" + newName);

		for (Path p : paths)
			if (p.getFile().equals(newFile))
				return false;

		File oldFile = path.getFile();
		path.setFile(newFile);
		oldFile.renameTo(newFile);
		doc.onPathTilesChanged();

		return true;
	}

	public void remove(Path path) {
		paths.remove(path);
		path.getFile().delete();
		doc.onPathTilesChanged();
	}

	public void split(Path path, LocationX pm) {
		List<LocationX> pms = path.split(pm);
		Path newPath = createPath(pms, getNewPathName());
		Saver.save(newPath);
		doc.onPathTilesChanged();
	}

	public Collection<Path> getPaths() {
		return paths;
	}

	private static String getNewPathName() {
		String s = new Date().toLocaleString();
		s = s.replace('.', '_');
		s = s.replace(':', '_');
		s = s.replace(" ", "__");
		return s;
	}

	public void dispose() {
		paths.clear();
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Paths");
		super.finalize();
	}
}
