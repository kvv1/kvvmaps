package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Register implements Serializable {
	public String name;
	public String controller;
	public int addr;
	public int register;

	public Register() {
	}

	public Register(String name, String controller, int addr, int register) {
		this.name = name;
		this.controller = controller;
		this.addr = addr;
		this.register = register;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Register other = (Register) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
