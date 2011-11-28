package kvv.kvvmap.common.maps;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kvv.kvvmap.adapter.Adapter;

public class MapsDir {
	private final Map<String, MapDir> maps = new HashMap<String, MapDir>();

	public MapsDir() {
		File[] files = new File(Adapter.MAPS_ROOT).listFiles();
		for (File file : files) {
			if (file.getName().endsWith(".dir")) {
				String name = file.getName().substring(0,
						file.getName().lastIndexOf('.'));
				try {
					maps.put(name, new MapDir(file));
					Adapter.log("mapDir " + name + " loaded");
					Adapter.logMem();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Set<String> names() {
		return maps.keySet();
	}

	public MapDir get(String name) {
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
