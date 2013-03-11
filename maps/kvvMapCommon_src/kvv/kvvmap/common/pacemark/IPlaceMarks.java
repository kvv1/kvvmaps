package kvv.kvvmap.common.pacemark;

import java.io.IOException;
import java.util.List;

import kvv.kvvmap.adapter.LocationX;

public interface IPlaceMarks {
	List<LocationX> getPlaceMarks();
	void save() throws IOException;
}
