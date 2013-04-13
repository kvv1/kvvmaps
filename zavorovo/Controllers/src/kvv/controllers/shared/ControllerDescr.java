package kvv.controllers.shared;

import java.io.Serializable;

public class ControllerDescr implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		TYPE1,
		TYPE2,
	}

	public ControllerDescr() {
	}

	public String name;
	public int addr;
	public Type type;
}
