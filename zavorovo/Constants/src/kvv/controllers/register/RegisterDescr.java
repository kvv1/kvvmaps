package kvv.controllers.register;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RegisterDescr implements Serializable {
	public int reg;
	public String name;
	public String text;
	public boolean readonly;
	public boolean editable;
	public RegType type;

	public RegisterDescr() {
	}

	public RegisterDescr(int reg) {
		this.reg = reg;
	}

	public RegisterDescr(int reg, boolean readonly, boolean editable) {
		this.reg = reg;
		this.readonly = readonly;
		this.editable = editable;
	}

	public RegisterDescr(RegisterDescr descr, String name, String text, RegType type) {
		this.reg = descr.reg;
		this.readonly = descr.readonly;
		this.editable = descr.editable;
		this.type = type;
		this.text = text;
		this.name = name;
	}
}
