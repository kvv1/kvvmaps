package kvv.kvvmap.common.maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.kvvmap.adapter.Adapter;

public class MapsDir {

	public static interface MapsDirListener {
		void mapAdded(String name);
	}

	private final Map<String, MapDir[]> maps = new HashMap<String, MapDir[]>();
	private MapsDirListener l;

	public MapsDir() {
	}

	public MapsDir(File[] files) {
		try {
			for (File file : files)
				addMap(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addMap(File file) throws IOException {
		MapDir[] dirs = read(file);
		addMap(dirs, getName(file));
	}

	public String getName(File file) {
		int ptidx = file.getName().lastIndexOf('.');
		if (ptidx < 0)
			ptidx = file.getName().length();
		return file.getName().substring(0, ptidx);
	}

	public void addMap(MapDir[] dirs, String name) {
		Adapter.log("mapDir " + name + " loading...");
		if (dirs != null) {
			maps.put(name, dirs);
			Adapter.log("mapDir " + name + " loaded");
			if (l != null)
				l.mapAdded(name);
			Adapter.logMem();
		}
	}

	public static MapDir[] read(File file) throws IOException {
		List<MapDir> res = new ArrayList<MapDir>();

		if (file.getName().endsWith(".dir")) {
			res.add(new MapDir(file, false));
		} else if (file.getName().endsWith(".kvvmap")) {
			res.add(new MapDir(file, true));
		} else if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				if (f.getName().endsWith(".dir"))
					res.add(new MapDir(f, false));
				else if(f.getName().endsWith(".kvvmap"))
					res.add(new MapDir(f, true));
			}
		}
		if (res.isEmpty())
			return null;
		return res.toArray(new MapDir[res.size()]);
	}

	public Set<String> names() {
		return maps.keySet();
	}

	public MapDir[] get(String name) {
		return maps.get(name);
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~MapsDir");
		super.finalize();
	}

	public void dispose() {
		maps.clear();
	}

	public void setListener(MapsDirListener l) {
		this.l = l;
	}

}
