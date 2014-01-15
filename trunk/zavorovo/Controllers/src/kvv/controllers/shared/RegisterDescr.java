package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RegisterDescr implements Serializable {
	public String name;
	public String controller;
	public int addr;
	public int register;
	public int[] scaleLevels;
	public Integer height;
	
	public int hysteresis;

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((RegisterDescr) obj).name);
	}

}
