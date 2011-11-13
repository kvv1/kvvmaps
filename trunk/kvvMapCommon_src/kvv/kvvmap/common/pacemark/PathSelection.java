/**
 * 
 */
package kvv.kvvmap.common.pacemark;

import kvv.kvvmap.adapter.LocationX;


public class PathSelection implements ISelectable {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((pm == null) ? 0 : pm.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathSelection other = (PathSelection) obj;
		return path == other.path && pm == other.pm;
	}
	
	public final Path path;
	public final LocationX pm;
	
	public PathSelection(Path path, LocationX pm) {
		super();
		this.path = path;
		this.pm = pm;
	}
}