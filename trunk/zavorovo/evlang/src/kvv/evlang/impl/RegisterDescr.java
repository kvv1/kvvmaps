package kvv.evlang.impl;

public class RegisterDescr {
	public Type type;
	public short reg;
	public boolean readonly;
	public boolean editable;
	public Short initValue;

	public RegisterDescr(Type type, int reg) {
		this.type = type;
		this.reg = (short) reg;
	}

	public RegisterDescr(Type type, int reg, boolean readonly, boolean editable,
			Short initValue) {
		this.type = type;
		this.reg = (short) reg;
		this.readonly = readonly;
		this.editable = editable;
		this.initValue = initValue;
	}

	public RegisterDescr(Type type, int reg, boolean readonly, boolean editable) {
		this(type, reg, readonly, editable, null);
	}
}
