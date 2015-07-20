package kvv.controller.register;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RegisterUI implements Serializable {
	public int reg;
	public String text;
	public RegType type;

	public RegisterUI() {
	}

	public RegisterUI(int reg, RegType type, String text) {
		this.reg = reg;
		this.type = type;
		this.text = text;
	}
}
