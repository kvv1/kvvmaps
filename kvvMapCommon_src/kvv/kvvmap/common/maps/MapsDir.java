package kvv.kvvmap.common.maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.kvvmap.adapter.Adapter;

public class MapsDir {
	private final Map<String, MapDir[]> maps = new HashMap<String, MapDir[]>();

	public MapsDir() throws IOException {
		File[] files = new File(Adapter.MAPS_ROOT).listFiles();
		for (File file : files) {
			List<MapDir> res = new ArrayList<MapDir>();
			int ptidx = file.getName().lastIndexOf('.');
			if (ptidx < 0)
				ptidx = file.getName().length();

			String name = file.getName().substring(0, ptidx);
			if (file.getName().endsWith(".dir")) {
				res.add(new MapDir(file));
			} else if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					if (f.getName().endsWith(".dir"))
						res.add(new MapDir(f));
				}
			}
			if (res.size() > 0) {
				maps.put(name, res.toArray(new MapDir[res.size()]));
				Adapter.log("mapDir " + name + " loaded");
				Adapter.logMem();
			}
		}
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

}
