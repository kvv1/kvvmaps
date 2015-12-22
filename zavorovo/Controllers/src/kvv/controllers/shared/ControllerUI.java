package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ControllerUI implements Serializable {
	public ControlType type;
	public Align align;
	public int reg;
	public String regName;
	public String label;
	public ControllerUI[] children;

	public enum Align {
		CENTER, BEGIN, END
	}

	public enum ControlType {
		CHECKBOX, TEXT_RO, TEXT_RO_TIME, TEXT_RW, TEXT2_RW, HP, VP, HELLO, UPLOAD, RULES, DETAILS, VM, RESET,
	}
}
