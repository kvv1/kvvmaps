package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.common.maps.Maps;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;

public class Environment {
	public Environment(Adapter adapter, Paths paths, PlaceMarks placemarks,
			Maps maps) {
		super();
		this.adapter = adapter;
		this.paths = paths;
		this.placemarks = placemarks;
		this.maps = maps;
	}
	public final Adapter adapter;
	public final Paths paths;
	public final PlaceMarks placemarks;
	public final Maps maps;
	
	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Environment");
		super.finalize();
	}

}