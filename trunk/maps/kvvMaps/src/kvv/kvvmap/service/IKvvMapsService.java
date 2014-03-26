package kvv.kvvmap.service;

import kvv.kvvmap.maps.MapsDir;
import kvv.kvvmap.placemark.Paths;
import kvv.kvvmap.placemark.PlaceMarks;
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