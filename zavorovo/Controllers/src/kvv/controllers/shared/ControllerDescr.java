package kvv.controllers.shared;

import java.io.Serializable;

public class ControllerDescr implements Serializable {
	private static final long serialVersionUID = 1L;

	public ControllerDescr() {
	}

	public String name;
	public int addr;
	public String type;
	public RegisterDescr[] registers;
}
