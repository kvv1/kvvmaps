package kvv.controllers.shared;

import java.io.Serializable;

public class ObjectDescr implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		HOTHOUSE,
		RELAY,
		SEPARATOR,
	}

	public ObjectDescr() {
	}

	public String name;
	public String controller;
	public int addr;
	public int register;
	public Type type;
}
