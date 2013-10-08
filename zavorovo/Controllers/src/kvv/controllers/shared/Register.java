package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Register implements Serializable {
	public String name;
	public String controller;
	public int addr;
	public int register;
	public int[] scaleLevels;
	public Integer height;

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Register) obj).name);
	}

}
