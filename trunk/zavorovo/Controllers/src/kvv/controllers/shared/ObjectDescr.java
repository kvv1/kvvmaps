package kvv.controllers.shared;

import java.io.Serializable;

public class ObjectDescr implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		FORM, RELAY,
	}

	public ObjectDescr() {
	}

	public String name;
	public String controller;
	public int addr;
	public String register;
	public int reg;
	public Type type;
}
