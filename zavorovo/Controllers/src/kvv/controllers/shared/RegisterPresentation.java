package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RegisterPresentation implements Serializable {
	public String name;
	public Integer min;
	public Integer max;
	public Integer step;
	public Integer height;

	public final boolean isBool() {
		return min == null || max == null || step == null;
	}
}
