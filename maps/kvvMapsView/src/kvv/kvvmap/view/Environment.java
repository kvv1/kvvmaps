package kvv.kvvmap.view;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.maps.Maps;
import kvv.kvvmap.placemark.Paths;
import kvv.kvvmap.placemark.PlaceMarks;

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
