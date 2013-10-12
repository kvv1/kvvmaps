package kvv.evlang.impl;

public class RegisterDescr {
	public int reg;
	public String name;
	public boolean readonly;
	public boolean editable;
	public Short initValue;

	public RegisterDescr(int reg) {
		this.reg = reg;
	}

	public RegisterDescr(int reg, boolean readonly, boolean editable,
			Short initValue) {
		this.reg = reg;
		this.readonly = readonly;
		this.editable = editable;
		this.initValue = initValue;
	}

	public RegisterDescr(int reg, boolean readonly, boolean editable) {
		this(reg, readonly, editable, null);
	}
}
