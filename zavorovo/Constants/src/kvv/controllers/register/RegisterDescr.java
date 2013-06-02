package kvv.controllers.register;

public class RegisterDescr {
	public int reg;
	public String name;
	public boolean readonly;
	public boolean editable;

	public RegisterDescr(int reg) {
		this.reg = reg;
	}

	public RegisterDescr(int reg, boolean readonly, boolean editable) {
		this.reg = reg;
		this.readonly = readonly;
		this.editable = editable;
	}

}
