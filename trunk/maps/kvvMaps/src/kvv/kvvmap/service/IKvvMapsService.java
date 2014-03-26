package kvv.kvvmap.service;

import kvv.kvvmap.common.maps.MapsDir;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import kvv.kvvmap.service.KvvMapsService.KvvMapsServiceListener;
import android.os.Bundle;

public interface IKvvMapsService {
	Tracker getTracker();

	Paths getPaths();

	MapsDir getMapsDir();

	PlaceMarks getPlacemarks();

	Bundle getBundle();

	boolean isLoadingMaps();

	void disconnect();

	void setListener(KvvMapsServiceListener l);
}