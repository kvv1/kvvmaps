package kvv.controllers.shared;

import java.io.Serializable;

public class ControllerDescr implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		TYPE2,
		MU110_8
	}

	public ControllerDescr() {
	}

	public String name;
	public int addr;
	public Type type;
}
