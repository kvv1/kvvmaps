package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.common.maps.Maps;
import kvv.kvvmap.common.maps.MapsDir;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import kvv.kvvmap.common.pacemark.Saver;

public class Environment {
	public Environment(Adapter adapter, Paths paths, PlaceMarks placemarks,
			Maps maps, MapsDir mapsDir) {
		super();
		this.adapter = adapter;
		this.paths = paths;
		this.placemarks = placemarks;
		this.mapsDir = mapsDir;
		this.maps = maps;
	}
	public Adapter adapter;
	public Paths paths;
	public PlaceMarks placemarks;
	public MapsDir mapsDir;
	public Maps maps;
	
	public void dispose() {
		Saver.dispose();
		
		Adapter.log("Environment.dispose");
		//adapter.dispose();
		maps.dispose();
	}
	
	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Environment");
		super.finalize();
	}
}
