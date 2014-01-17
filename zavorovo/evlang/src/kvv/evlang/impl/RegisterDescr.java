package kvv.evlang.impl;

public class RegisterDescr {
	public Type type;
	public int reg;
	public boolean readonly;
	public boolean editable;
	public Short initValue;

	public RegisterDescr(Type type, int reg) {
		this.type = type;
		this.reg = reg;
	}

	public RegisterDescr(Type type, int reg, boolean readonly, boolean editable,
			Short initValue) {
		this.type = type;
		this.reg = reg;
		this.readonly = readonly;
		this.editable = editable;
		this.initValue = initValue;
	}

	public RegisterDescr(Type type, int reg, boolean readonly, boolean editable) {
		this(type, reg, readonly, editable, null);
	}
}
