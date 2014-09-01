package kvv.kvvmap.view;

import java.util.List;

import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.util.ISelectable;
import kvv.kvvmap.util.InfoLevel;
import kvv.kvvmap.view.CommonView.RotationMode;

public interface ICommonView {
	void setZoom(int zoom);
	int getZoom();
	void zoomIn();
	void zoomOut();
	
	String getTopMap();
	void setTopMap(String map);
	void fixMap(String map);
	List<String> getCenterMaps();
	void reorderMaps();
	
	ISelectable getSel();
	
	void onSizeChanged(int w, int h);
	
	void draw(GC gc);
	
	void startScrolling();
	void endScrolling();
	void scrollBy(double dx, double dy);
	
	void animateTo(LocationX loc);
	void animateToTarget();
	LocationX getLocation();
	
	LocationX getMyLocation();
	void setMyLocation(LocationX loc, boolean scroll);
	boolean isOnMyLocation();
	void dimmMyLocation();
	void animateToMyLocation();
	
	void setRotationMode(RotationMode rotationMode);
	RotationMode getRotationMode();
	void setAngle(float deg);

	InfoLevel getInfoLevel();
	void setInfoLevel(InfoLevel level);
	void incInfoLevel();
	void decInfoLevel();
	
	LocationX getTarget();

	boolean isMultiple();
	
	void dispose();
}
