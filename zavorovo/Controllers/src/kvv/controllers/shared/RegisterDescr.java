package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RegisterDescr implements Serializable {
	public String name;
	public String controller;
	public Integer controllerAddr;
	public int register;
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((RegisterDescr) obj).name);
	}

}
