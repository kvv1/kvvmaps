package kvv.kvvmap.common.maps;

import java.util.Collection;

import kvv.kvvmap.common.Img;

public class MapDescrGroup extends MapDescrBase {

	private final Collection<MapDescr> maps;

	public MapDescrGroup(String name, Collection<MapDescr> maps) {
		super(name);
		this.maps = maps;
	}

	@Override
	protected boolean hasTile(int nx, int ny, int zoom) {
		for(MapDescr map : maps)
			if(map.hasTile(nx, ny, zoom))
				return true;
		return false;
	}

	@Override
	protected Img load(int nx, int ny, int zoom, int x, int y, int sz,
			Img imgBase) {
		for(MapDescr map : maps)
			if(map.hasTile(nx, ny, zoom))
				return map.load(nx, ny, zoom, x, y, sz, imgBase);
		return imgBase;
	}

}
